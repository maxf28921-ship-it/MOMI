package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.VoiceProfileType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) {

    private val tag = "NovaSpeechManager"

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private var isRecognizerActive = false

    private val _isSpeakingFlow = MutableStateFlow(false)
    val isSpeakingFlow: StateFlow<Boolean> = _isSpeakingFlow.asStateFlow()

    private val _rmsAmplitudeFlow = MutableStateFlow(0f)
    val rmsAmplitudeFlow: StateFlow<Float> = _rmsAmplitudeFlow.asStateFlow()

    private var currentVoiceProfile: VoiceProfileType = VoiceProfileType.HI_FEMALE
    private var customPitch: Float = 1.0f
    private var customRate: Float = 1.0f

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                applyVoiceProfile(currentVoiceProfile, customPitch, customRate)
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeakingFlow.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeakingFlow.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeakingFlow.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeakingFlow.value = false
                    }
                })
            } else {
                Log.e(tag, "TTS Initialization failed with status: $status")
            }
        }
    }

    fun applyVoiceProfile(profile: VoiceProfileType, pitchFactor: Float = 1.0f, rateFactor: Float = 1.0f) {
        currentVoiceProfile = profile
        customPitch = pitchFactor
        customRate = rateFactor

        if (!isTtsReady || textToSpeech == null) return

        val targetLocale = when (profile) {
            VoiceProfileType.HI_FEMALE, VoiceProfileType.HI_MALE -> Locale("hi", "IN")
            VoiceProfileType.BN_FEMALE, VoiceProfileType.BN_MALE -> Locale("bn", "IN")
            VoiceProfileType.EN_ASSISTANT -> Locale("en", "US")
        }

        val langResult = textToSpeech?.setLanguage(targetLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            // fallback to default
            textToSpeech?.setLanguage(Locale.getDefault())
        }

        val finalPitch = (profile.defaultPitch * pitchFactor).coerceIn(0.5f, 2.0f)
        val finalRate = (profile.defaultRate * rateFactor).coerceIn(0.5f, 2.0f)

        textToSpeech?.setPitch(finalPitch)
        textToSpeech?.setSpeechRate(finalRate)
    }

    fun speak(text: String, utteranceId: String = "nova_utterance_${System.currentTimeMillis()}") {
        if (!isTtsReady || textToSpeech == null) {
            Log.w(tag, "TTS not ready yet")
            return
        }
        stopSpeaking()

        // Clean out emojis or brackets for cleaner TTS reading if desired, or speak naturally
        val spokenText = text.replace(Regex("[📞💬📱🔔🌦️⏰🎵😄✨❤️]"), "")
            .trim()

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        _isSpeakingFlow.value = true
        textToSpeech?.speak(spokenText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        if (isTtsReady && textToSpeech != null) {
            textToSpeech?.stop()
        }
        _isSpeakingFlow.value = false
    }

    fun startListening(profile: VoiceProfileType) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onSpeechError?.invoke("Speech recognition is not available on this device.")
            return
        }

        stopListening()
        stopSpeaking()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isRecognizerActive = true
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB..10dB to 0f..1f
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _rmsAmplitudeFlow.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _rmsAmplitudeFlow.value = 0f
                    }

                    override fun onError(error: Int) {
                        isRecognizerActive = false
                        _rmsAmplitudeFlow.value = 0f
                        val errorMessage = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected within time."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition."
                            else -> "Voice recognition stopped (code $error)"
                        }
                        onSpeechError?.invoke(errorMessage)
                    }

                    override fun onResults(results: Bundle?) {
                        isRecognizerActive = false
                        _rmsAmplitudeFlow.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrEmpty()) {
                            onSpeechRecognized?.invoke(text)
                        } else {
                            onSpeechError?.invoke("Couldn't hear anything. Try again?")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()
                        if (!partial.isNullOrEmpty()) {
                            _rmsAmplitudeFlow.value = 0.7f
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                val langTag = when (profile) {
                    VoiceProfileType.HI_FEMALE, VoiceProfileType.HI_MALE -> "hi-IN"
                    VoiceProfileType.BN_FEMALE, VoiceProfileType.BN_MALE -> "bn-IN"
                    VoiceProfileType.EN_ASSISTANT -> "en-US"
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(tag, "Failed to start speech recognizer", e)
            onSpeechError?.invoke("Failed to start voice listener: ${e.message}")
        }
    }

    fun stopListening() {
        if (isRecognizerActive) {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            isRecognizerActive = false
            _rmsAmplitudeFlow.value = 0f
        }
    }

    fun destroy() {
        stopListening()
        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
