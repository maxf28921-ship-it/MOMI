package com.example.service

import com.example.BuildConfig
import com.example.model.VoiceProfileType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NovaBrain(
    private val geminiService: GeminiApiService = GeminiApiService()
) {

    suspend fun processQuery(
        query: String,
        userApiKey: String,
        voiceProfile: VoiceProfileType,
        recentHistory: List<Pair<String, Boolean>> = emptyList()
    ): String {
        val trimmed = query.trim()

        // 1. Try Gemini API if an API key is available and not the default placeholder
        val effectiveApiKey = when {
            userApiKey.isNotBlank() -> userApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (effectiveApiKey.isNotBlank()) {
            val geminiResult = geminiService.generateVoiceResponse(
                apiKey = effectiveApiKey,
                prompt = trimmed,
                voiceProfileId = voiceProfile.id,
                recentHistory = recentHistory
            )
            if (geminiResult.isSuccess) {
                return geminiResult.getOrThrow()
            }
        }

        // 2. Intelligent Offline Multilingual Fallback Engine
        return resolveOfflineQuery(trimmed, voiceProfile)
    }

    fun getQuickCommandReply(commandType: String, voiceProfile: VoiceProfileType): String {
        return when (commandType.lowercase()) {
            "call" -> when {
                voiceProfile.localeTag.startsWith("bn") -> "মা-কে কল করা হচ্ছে... Calling Mom now."
                voiceProfile.localeTag.startsWith("hi") -> "माँ को कॉल मिला रही हूँ... Calling Mom now."
                else -> "Calling Mom on mobile... (মা-কে কল করা হচ্ছে...)"
            }
            "message" -> when {
                voiceProfile.localeTag.startsWith("bn") -> "কাকে মেসেজ পাঠাতে চান? Who would you like to message?"
                voiceProfile.localeTag.startsWith("hi") -> "आप किसे संदेश भेजना चाहते हैं? Who would you like to message?"
                else -> "Who would you like to message? (কাকে মেসেজ পাঠাতে চান?)"
            }
            "apps" -> when {
                voiceProfile.localeTag.startsWith("bn") -> "হোয়াটসঅ্যাপ এবং ক্যামেরা প্রস্তুত করা হচ্ছে... Opening Apps."
                voiceProfile.localeTag.startsWith("hi") -> "व्हाट्सएप और कैमरा खोला जा रहा है... Opening Apps."
                else -> "Opening WhatsApp & Camera... (হোয়াটসঅ্যাপ এবং ক্যামেরা খোলা হচ্ছে...)"
            }
            "notifs" -> when {
                voiceProfile.localeTag.startsWith("bn") -> "আপনার ২টি নতুন নোটিফিকেশন আছে: ১টি মেসেজ এবং আবহাওয়ার সতর্কতা।"
                voiceProfile.localeTag.startsWith("hi") -> "आपके पास २ नए नोटिफिकेशन हैं: एक संदेश और मौसम की जानकारी।"
                else -> "You have 2 new notifications. (आपके पास २ नई सूचनाएं हैं।)"
            }
            "weather" -> when {
                voiceProfile.localeTag.startsWith("bn") -> "আজকের তাপমাত্রা ২৮ ডিগ্রি সেলসিয়াস, আকাশ রৌদ্রোজ্জ্বল থাকবে।"
                voiceProfile.localeTag.startsWith("hi") -> "आज तापमान २८ डिग्री सेल्सियस है और मौसम बिल्कुल साफ रहेगा।"
                else -> "Today's temperature is 28°C with pleasant clear skies."
            }
            "alarm" -> when {
                voiceProfile.localeTag.startsWith("bn") -> "সকাল ৭টার জন্য অ্যালার্ম ও রিমাইন্ডার সেট করে দেওয়া হলো।"
                voiceProfile.localeTag.startsWith("hi") -> "सुबह ७:०० बजे का अलार्म और रिमाइंडर सेट कर दिया गया है।"
                else -> "Reminder and alarm set for 7:00 AM."
            }
            "joke" -> getRandomJoke(voiceProfile)
            else -> "मैंने सुन लिया! बोलिए आगे क्या करना है? (আমি শুনে নিয়েছি! বলুন आगे কি করতে হবে?)"
        }
    }

    private fun resolveOfflineQuery(query: String, voiceProfile: VoiceProfileType): String {
        val q = query.lowercase(Locale.ROOT)
        val isBengaliPref = voiceProfile.localeTag.startsWith("bn")
        val isHindiPref = voiceProfile.localeTag.startsWith("hi")

        // Greetings
        if (q.contains("hello") || q.contains("hi") || q.contains("namaste") || q.contains("नमस्ते") ||
            q.contains("নমস্কার") || q.contains("সালাম") || q.contains("hey nova") || q.contains("सुनो")
        ) {
            return if (isBengaliPref) {
                "নমস্কার! আমি নোভা, আপনার ভয়েস অ্যাসিস্ট্যান্ট। বলুন আমি আপনাকে কীভাবে সাহায্য করতে পারি?"
            } else if (isHindiPref) {
                "नमस्ते! मैं नोवा हूँ, आपकी वॉइस कंपैनियन। बताइए, मैं आपकी क्या सहायता करूँ?"
            } else {
                "Hello! I am Nova, your voice companion. How can I assist you today?"
            }
        }

        // Who are you / Identity
        if (q.contains("who are you") || q.contains("tum kaun ho") || q.contains("तुम कौन हो") ||
            q.contains("তুমি কে") || q.contains("your name") || q.contains("नाम क्या है") || q.contains("নাম কি")
        ) {
            return if (isBengaliPref) {
                "আমি নোভা—আপনার নিজস্ব ভয়েস এআই কম্প্যানিয়ন। আমি কল করা, বার্তা পাঠানো এবং তথ্য দিতে সাহায্য করি।"
            } else if (isHindiPref) {
                "मैं नोवा हूँ—आपकी अपनी वॉइस एआई साथी। मैं कॉल करने, संदेश भेजने और आपकी हर बात सुनने के लिए तैयार हूँ।"
            } else {
                "I am Nova, your intelligent voice AI companion supporting Hindi, Bengali, and English."
            }
        }

        // How are you
        if (q.contains("how are you") || q.contains("kaisi ho") || q.contains("कैसी हो") ||
            q.contains("क्या हाल") || q.contains("কেমন আছো") || q.contains("kemon acho")
        ) {
            return if (isBengaliPref) {
                "আমি খুব ভালো আছি! আপনার সাথে কথা বলতে পেরে ভীষণ আনন্দ হচ্ছে। আপনি কেমন আছেন?"
            } else if (isHindiPref) {
                "मैं बहुत अच्छी हूँ! आपसे बात करके बहुत खुशी हो रही है। आप कैसे हैं?"
            } else {
                "I'm feeling wonderful and ready to help! How is your day going?"
            }
        }

        // Calling / Phone
        if (q.contains("call") || q.contains("phone") || q.contains("कॉल") || q.contains("फोन") ||
            q.contains("কল") || q.contains("ফোন") || q.contains("dial")
        ) {
            return if (isBengaliPref) {
                "অবশ্যই, আপনার পরিবারের সদস্যকে কল করার জন্য ডায়ালার খোলা হচ্ছে।"
            } else if (isHindiPref) {
                "बिल्कुल, कॉल लगाने के लिए फोन डायलर खोला जा रहा है।"
            } else {
                "Calling your favorite contact right away..."
            }
        }

        // Messaging
        if (q.contains("message") || q.contains("sms") || q.contains("संदेश") || q.contains("मैसेज") ||
            q.contains("মেসেজ") || q.contains("বার্তা") || q.contains("whatsapp")
        ) {
            return if (isBengaliPref) {
                "কাকে বার্তা পাঠাতে চান? বার্তা লেখার জন্য চ্যাট প্রস্তুত রয়েছে।"
            } else if (isHindiPref) {
                "आप किसे संदेश भेजना चाहते हैं? मैसेज बॉक्स तैयार है।"
            } else {
                "Who would you like to message? Ready to send your words."
            }
        }

        // Weather
        if (q.contains("weather") || q.contains("मौसम") || q.contains("barish") || q.contains("बारिश") ||
            q.contains("আবহাওয়া") || q.contains("বৃষ্টি") || q.contains("temp") || q.contains("तापमान")
        ) {
            return if (isBengaliPref) {
                "আজকের আবহাওয়া অত্যন্ত মনোরম। তাপমাত্রা প্রায় ২৮ ডিগ্রি সেলসিয়াস এবং বৃষ্টির সম্ভাবনা খুব কম।"
            } else if (isHindiPref) {
                "आज मौसम बहुत सुहावना है। तापमान २८ डिग्री सेल्सियस है और हल्की ठंडी हवा चल रही है।"
            } else {
                "The weather today is clear and pleasant with temperatures around 28°C."
            }
        }

        // Time / Date
        if (q.contains("time") || q.contains("समय") || q.contains("वक्त") || q.contains("कितने बजे") ||
            q.contains("সময়") || q.contains("কটা বাজে") || q.contains("date") || q.contains("तारीख")
        ) {
            val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            return if (isBengaliPref) {
                "এখন সময় হলো প্রায় $timeFmt। দিনটি ভালো কাটুক!"
            } else if (isHindiPref) {
                "इस समय घड़ी में $timeFmt बजे हैं। आपका दिन शुभ हो!"
            } else {
                "The current time is $timeFmt. Hope you are having a productive day!"
            }
        }

        // Joke
        if (q.contains("joke") || q.contains("चुटकुला") || q.contains("हंसाओ") ||
            q.contains("কৌতুক") || q.contains("হাসাও") || q.contains("funny")
        ) {
            return getRandomJoke(voiceProfile)
        }

        // What can you do / Help
        if (q.contains("what can you do") || q.contains("help") || q.contains("मदद") ||
            q.contains("क्या कर सकती") || q.contains("কি করতে পারো") || q.contains("সাহায্য")
        ) {
            return if (isBengaliPref) {
                "আমি ফোন কল, মেসেজ, আবহাওয়ার খবর, অ্যালার্ম সেট করা এবং বাংলা ও হিন্দিতে ভয়েস চ্যাট করতে পারি।"
            } else if (isHindiPref) {
                "मैं कॉल लगाने, मैसेज भेजने, मौसम बताने, अलार्म सेट करने और हिंदी-बंगाली में बातें करने में सक्षम हूँ।"
            } else {
                "I can make phone calls, send messages, give weather updates, tell jokes, and chat in Hindi, Bengali, or English!"
            }
        }

        // Compliments
        if (q.contains("good") || q.contains("great") || q.contains("smart") || q.contains("बहुत अच्छी") ||
            q.contains("দারুণ") || q.contains("ধন্যবাদ") || q.contains("thank") || q.contains("शुक्रिया")
        ) {
            return if (isBengaliPref) {
                "অনেক অনেক ধন্যবাদ! আপনার সাথে কথা বলতে পেরে আমার খুব ভালো লাগলো।"
            } else if (isHindiPref) {
                "बहुत-बहुत धन्यवाद! आपकी प्रशंसा सुनकर मुझे बहुत खुशी हुई।"
            } else {
                "Thank you so much! It's an honor to be your assistant."
            }
        }

        // Default conversational answer
        return if (isBengaliPref) {
            "আমি শুনে নিয়েছি: \"$query\"। আপনি কি এটি সম্পর্কে আরও বিস্তারিত জানতে চান?"
        } else if (isHindiPref) {
            "मैंने आपकी बात सुन ली: \"$query\"। क्या आप इस बारे में और कुछ जानना चाहते हैं?"
        } else {
            "I heard you clearly: \"$query\". Let me know how else I can assist you!"
        }
    }

    private fun getRandomJoke(voiceProfile: VoiceProfileType): String {
        val hindiJokes = listOf(
            "शिक्षक ने छात्र से पूछा: संजू, न्यूटन का चौथा नियम क्या है? संजू बोला: सर, जब परीक्षा कठिन हो, तो भगवान ही एकमात्र सहारा हैं!",
            "डॉक्टर: आपको आराम की सख्त जरूरत है। मरीज: लेकिन डॉक्टर साहब, मैं तो दिन भर सोता ही रहता हूँ! डॉक्टर: तो फिर नींद में थोड़ा आराम कम करो!",
            "पप्पू: भाई तेरा फोन साइलेंट पर क्यों रहता है हमेशा? गप्पू: ताकि जब फोन बजे तो मुझे डिस्टर्ब न हो!"
        )
        val bengaliJokes = listOf(
            "শিক্ষক: মন্টু, নিউটনের চতুর্থ সূত্রটি কি? মন্টু: স্যার, যখন প্রশ্ন খুব কঠিন হয়, তখন প্রার্থনাই একমাত্র গতি!",
            "ডাক্তার: আপনার পর্যাপ্ত বিশ্রামের প্রয়োজন। রোগী: কিন্তু ডাক্তারবাবু আমি তো সারাদিন ঘুমিয়েই কাটাই! ডাক্তার: তাহলে ঘুমের মাঝে একটু কম বিশ্রাম নিন!",
            "পল্টু: ভাই তোর ফোন সারাদিন সাইলেন্ট থাকে কেন? বল্টু: যাতে ফোন বাজলে আমার মনোযোগ নষ্ট না হয়!"
        )
        val englishJokes = listOf(
            "Why don't scientists trust atoms? Because they make up everything!",
            "What did one wall say to the other wall? I'll meet you at the corner!",
            "Why did the smartphone need glasses? Because it lost all its contacts!"
        )

        return when {
            voiceProfile.localeTag.startsWith("bn") -> bengaliJokes.random()
            voiceProfile.localeTag.startsWith("hi") -> hindiJokes.random()
            else -> englishJokes.random()
        }
    }
}
