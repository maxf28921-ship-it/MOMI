package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    suspend fun generateVoiceResponse(
        apiKey: String,
        prompt: String,
        voiceProfileId: String,
        recentHistory: List<Pair<String, Boolean>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val systemPrompt = """
                You are Nova, an intelligent, warm, polite, and culturally intuitive voice AI companion designed for Android.
                You are exceptionally fluent in Hindi (हिंदी), Bengali (বাংলা), and English.
                Rules for responses:
                1. Keep answers concise, natural, conversational, and direct (1-3 sentences maximum), suitable for voice Text-To-Speech read-out.
                2. If the user speaks in Hindi, respond in natural, graceful Hindi using Devanagari script.
                3. If the user speaks in Bengali, respond in friendly, authentic Bengali using Bengali script.
                4. If the user speaks in English or mixed Hinglish/Banglish, respond appropriately and helpfully.
                5. Do NOT include markdown styling like asterisks (**bold**), bullet points, or raw URL links since this text will be read aloud.
                6. When answering action commands (like call, message, reminder), acknowledge clearly.
            """.trimIndent()

            val contentsArray = JSONArray()

            // Include recent conversational turns (last 4)
            recentHistory.takeLast(4).forEach { (msgText, isUser) ->
                val turnObj = JSONObject().apply {
                    put("role", if (isUser) "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", msgText)))
                }
                contentsArray.put(turnObj)
            }

            // Current prompt
            val currentTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            }
            contentsArray.put(currentTurn)

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 250)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                val errorMsg = responseBody ?: "HTTP ${response.code}"
                return@withContext Result.failure(Exception("Gemini error: $errorMsg"))
            }

            val jsonObject = JSONObject(responseBody)
            val candidates = jsonObject.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("Empty candidate response from Gemini"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
