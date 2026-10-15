package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AssistantStatus
import com.example.model.ChatMessage
import com.example.model.VoiceProfileType
import com.example.service.NovaBrain
import com.example.service.SpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NovaUiState(
    val status: AssistantStatus = AssistantStatus.IDLE,
    val currentResponse: String = "नमस्ते! बोलिए, मैं आपकी क्या सहायता करूँ? (নমস্কার! বলুন, আমি কীভাবে সাহায্য করতে পারি?)",
    val lastUserQuery: String? = null,
    val voiceProfile: VoiceProfileType = VoiceProfileType.HI_FEMALE,
    val apiKey: String = "",
    val pitchFactor: Float = 1.0f,
    val rateFactor: Float = 1.0f,
    val audioAmplitude: Float = 0f,
    val messages: List<ChatMessage> = emptyList(),
    val isSettingsOpen: Boolean = false,
    val isHistoryOpen: Boolean = false,
    val actionNotification: String? = null
)

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("nova_assistant_prefs", Context.MODE_PRIVATE)
    private val speechManager = SpeechManager(application)
    private val novaBrain = NovaBrain()

    private val _uiState = MutableStateFlow(NovaUiState())
    val uiState: StateFlow<NovaUiState> = _uiState.asStateFlow()

    private var speechJob: Job? = null
    private var listeningTimeoutJob: Job? = null

    init {
        // Load stored preferences
        val savedVoiceId = prefs.getString("nova_voice_profile", VoiceProfileType.HI_FEMALE.id) ?: VoiceProfileType.HI_FEMALE.id
        val profile = VoiceProfileType.fromId(savedVoiceId)
        val savedApiKey = prefs.getString("nova_api_key", "") ?: ""
        val savedPitch = prefs.getFloat("nova_pitch", 1.0f)
        val savedRate = prefs.getFloat("nova_rate", 1.0f)

        _uiState.update {
            it.copy(
                voiceProfile = profile,
                apiKey = savedApiKey,
                pitchFactor = savedPitch,
                rateFactor = savedRate
            )
        }

        speechManager.applyVoiceProfile(profile, savedPitch, savedRate)

        // Observe TTS state
        viewModelScope.launch {
            speechManager.isSpeakingFlow.collect { isSpeaking ->
                if (!isSpeaking && _uiState.value.status == AssistantStatus.SPEAKING) {
                    _uiState.update { it.copy(status = AssistantStatus.IDLE) }
                }
            }
        }

        // Observe audio amplitude
        viewModelScope.launch {
            speechManager.rmsAmplitudeFlow.collect { amp ->
                if (_uiState.value.status == AssistantStatus.LISTENING) {
                    _uiState.update { it.copy(audioAmplitude = amp) }
                } else if (_uiState.value.status != AssistantStatus.SPEAKING) {
                    _uiState.update { it.copy(audioAmplitude = 0f) }
                }
            }
        }

        // Setup speech recognition callbacks
        speechManager.onSpeechRecognized = { text ->
            onUserSpeechRecognized(text)
        }

        speechManager.onSpeechError = { errorText ->
            Log.w("NovaViewModel", "Speech recognition error: $errorText")
            // If recognizer had no match or failed, gracefully fall back
            if (_uiState.value.status == AssistantStatus.LISTENING) {
                _uiState.update { it.copy(status = AssistantStatus.IDLE, audioAmplitude = 0f) }
            }
        }
    }

    fun toggleListening(hasAudioPermission: Boolean) {
        val currentStatus = _uiState.value.status
        if (currentStatus == AssistantStatus.LISTENING) {
            // Stop listening
            stopListening()
            // Provide response if waiting
            processQuery("Hello Nova")
        } else {
            speechManager.stopSpeaking()
            _uiState.update { it.copy(status = AssistantStatus.LISTENING) }

            if (hasAudioPermission) {
                speechManager.startListening(_uiState.value.voiceProfile)
            } else {
                // Speech simulation timer for emulator without physical mic
                startListeningSimulation()
            }
        }
    }

    private fun startListeningSimulation() {
        listeningTimeoutJob?.cancel()
        listeningTimeoutJob = viewModelScope.launch {
            // Pulse wave animation
            for (i in 1..7) {
                delay(400)
                if (_uiState.value.status != AssistantStatus.LISTENING) return@launch
                val wave = if (i % 2 == 0) 0.85f else 0.45f
                _uiState.update { it.copy(audioAmplitude = wave) }
            }
            if (_uiState.value.status == AssistantStatus.LISTENING) {
                _uiState.update { it.copy(status = AssistantStatus.IDLE, audioAmplitude = 0f) }
                val reply = "मैंने सुन लिया! बोलिए आगे क्या करना है? (আমি শুনে নিয়েছি! বলুন आगे কি করতে হবে?)"
                setAssistantResponse(reply, "Voice Prompt")
            }
        }
    }

    fun onUserSpeechRecognized(query: String) {
        listeningTimeoutJob?.cancel()
        stopListening()
        processQuery(query)
    }

    fun processQuery(query: String) {
        if (query.isBlank()) return
        speechManager.stopSpeaking()

        val profile = _uiState.value.voiceProfile
        val apiKey = _uiState.value.apiKey

        val userMessage = ChatMessage(text = query, isUser = true)
        val historyList = _uiState.value.messages.map { it.text to it.isUser }

        _uiState.update {
            it.copy(
                status = AssistantStatus.THINKING,
                lastUserQuery = query,
                messages = it.messages + userMessage,
                audioAmplitude = 0.2f
            )
        }

        speechJob?.cancel()
        speechJob = viewModelScope.launch {
            val responseText = novaBrain.processQuery(query, apiKey, profile, historyList)
            setAssistantResponse(responseText, query)
        }
    }

    fun triggerQuickAction(actionType: String) {
        speechManager.stopSpeaking()
        val profile = _uiState.value.voiceProfile
        val reply = novaBrain.getQuickCommandReply(actionType, profile)

        val promptLabel = when (actionType) {
            "call" -> "📞 Call Family"
            "message" -> "💬 Send Message"
            "apps" -> "📱 Open Apps"
            "notifs" -> "🔔 Read Alerts"
            "weather" -> "🌦️ Weather"
            "alarm" -> "⏰ Set Alarm"
            "joke" -> "😄 Tell a Joke"
            else -> actionType
        }

        setAssistantResponse(reply, promptLabel, actionType)
        handleActionIntent(actionType)
    }

    private fun handleActionIntent(actionType: String) {
        val context = getApplication<Application>()
        try {
            when (actionType) {
                "call" -> {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                "message" -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                "apps" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                "alarm" -> {
                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_MESSAGE, "Nova Alarm")
                        putExtra(AlarmClock.EXTRA_HOUR, 7)
                        putExtra(AlarmClock.EXTRA_MINUTES, 0)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("NovaViewModel", "Intent execution failed gracefully: ${e.message}")
        }
    }

    private fun setAssistantResponse(reply: String, query: String, actionType: String? = null) {
        val assistantMessage = ChatMessage(
            text = reply,
            isUser = false,
            actionType = actionType
        )

        _uiState.update {
            it.copy(
                status = AssistantStatus.SPEAKING,
                currentResponse = reply,
                messages = it.messages + assistantMessage,
                audioAmplitude = 0.8f
            )
        }

        speechManager.speak(reply)
    }

    fun repeatSpeech() {
        val currentText = _uiState.value.currentResponse
        if (currentText.isNotBlank()) {
            _uiState.update { it.copy(status = AssistantStatus.SPEAKING) }
            speechManager.speak(currentText)
        }
    }

    fun stopSpeaking() {
        speechManager.stopSpeaking()
        _uiState.update { it.copy(status = AssistantStatus.IDLE, audioAmplitude = 0f) }
    }

    fun stopListening() {
        listeningTimeoutJob?.cancel()
        speechManager.stopListening()
        _uiState.update { it.copy(status = AssistantStatus.IDLE, audioAmplitude = 0f) }
    }

    fun updateVoiceProfile(profile: VoiceProfileType) {
        prefs.edit().putString("nova_voice_profile", profile.id).apply()
        _uiState.update { it.copy(voiceProfile = profile) }
        speechManager.applyVoiceProfile(profile, _uiState.value.pitchFactor, _uiState.value.rateFactor)

        val confirmation = when {
            profile.localeTag.startsWith("bn") -> "আওয়াজ পরিবর্তন করা হয়েছে।"
            profile.localeTag.startsWith("hi") -> "आवाज़ बदल दी गई है।"
            else -> "Voice updated successfully."
        }
        speechManager.speak(confirmation)
    }

    fun updateVoiceSliders(pitch: Float, rate: Float) {
        prefs.edit().putFloat("nova_pitch", pitch).putFloat("nova_rate", rate).apply()
        _uiState.update { it.copy(pitchFactor = pitch, rateFactor = rate) }
        speechManager.applyVoiceProfile(_uiState.value.voiceProfile, pitch, rate)
    }

    fun saveApiKey(key: String) {
        val clean = key.trim()
        prefs.edit().putString("nova_api_key", clean).apply()
        _uiState.update { it.copy(apiKey = clean) }
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
    }

    fun setHistoryOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = isOpen) }
    }

    fun clearHistory() {
        _uiState.update { it.copy(messages = emptyList()) }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
