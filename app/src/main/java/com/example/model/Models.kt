package com.example.model

import java.util.Locale
import java.util.UUID

enum class AssistantStatus {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

enum class VoiceProfileType(
    val id: String,
    val title: String,
    val subtitle: String,
    val localeTag: String,
    val defaultPitch: Float,
    val defaultRate: Float,
    val isFemale: Boolean
) {
    HI_FEMALE(
        id = "hi-female",
        title = "Hindi Female Voice",
        subtitle = "हिंदी महिला आवाज़",
        localeTag = "hi-IN",
        defaultPitch = 1.25f,
        defaultRate = 1.02f,
        isFemale = true
    ),
    HI_MALE(
        id = "hi-male",
        title = "Hindi Male Voice",
        subtitle = "हिंदी पुरुष आवाज़",
        localeTag = "hi-IN",
        defaultPitch = 0.82f,
        defaultRate = 0.95f,
        isFemale = false
    ),
    BN_FEMALE(
        id = "bn-female",
        title = "Bangla Female Voice",
        subtitle = "বাংলা মহিলা কণ্ঠ",
        localeTag = "bn-IN",
        defaultPitch = 1.25f,
        defaultRate = 1.02f,
        isFemale = true
    ),
    BN_MALE(
        id = "bn-male",
        title = "Bangla Male Voice",
        subtitle = "বাংলা পুরুষ কণ্ঠ",
        localeTag = "bn-IN",
        defaultPitch = 0.82f,
        defaultRate = 0.95f,
        isFemale = false
    ),
    EN_ASSISTANT(
        id = "en-assistant",
        title = "English Companion",
        subtitle = "English Voice",
        localeTag = "en-US",
        defaultPitch = 1.05f,
        defaultRate = 1.0f,
        isFemale = true
    );

    companion object {
        fun fromId(id: String): VoiceProfileType {
            return entries.find { it.id == id } ?: HI_FEMALE
        }
    }
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val detectedLang: String = "hi",
    val actionType: String? = null
)

data class QuickActionItem(
    val id: String,
    val iconEmoji: String,
    val titleEn: String,
    val titleNative: String,
    val commandPrompt: String
)
