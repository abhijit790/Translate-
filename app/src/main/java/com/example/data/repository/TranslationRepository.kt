package com.example.data.repository

import com.example.data.dao.CustomTranslationDao
import com.example.data.dao.DictionaryWordDao
import com.example.data.dao.LanguagePackDao
import com.example.data.dao.TranslationHistoryDao
import com.example.data.model.CustomTranslation
import com.example.data.model.DictionaryWord
import com.example.data.model.LanguagePack
import com.example.data.model.TranslationHistory
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class TranslationRepository(
    private val historyDao: TranslationHistoryDao,
    private val dictionaryDao: DictionaryWordDao,
    private val customDao: CustomTranslationDao,
    private val packDao: LanguagePackDao
) {
    val allHistory: Flow<List<TranslationHistory>> = historyDao.getAllHistory()
    val bookmarks: Flow<List<TranslationHistory>> = historyDao.getBookmarks()
    val allPacks: Flow<List<LanguagePack>> = packDao.getAllPacks()
    val customTranslations: Flow<List<CustomTranslation>> = customDao.getAllCustomTranslations()

    // Default language packs
    suspend fun initializeLanguagePacks() {
        val initialPacks = listOf(
            LanguagePack("en", "English (Base)", true, 0.0f),
            LanguagePack("bn", "Bengali", false, 32.4f),
            LanguagePack("hi", "Hindi", false, 28.1f),
            LanguagePack("es", "Spanish", false, 24.6f)
        )
        packDao.insertPacks(initialPacks)
    }

    suspend fun updatePack(pack: LanguagePack) {
        packDao.updatePack(pack)
    }

    suspend fun getPackByCode(code: String): LanguagePack? = packDao.getPackByCode(code)

    suspend fun insertHistory(history: TranslationHistory) {
         historyDao.insertHistory(history)
    }

    suspend fun updateHistory(history: TranslationHistory) {
         historyDao.updateHistory(history)
    }

    suspend fun deleteHistory(history: TranslationHistory) {
         historyDao.deleteHistory(history)
    }

    suspend fun clearHistory() {
         historyDao.clearAllHistory()
    }

    suspend fun insertCustomTranslation(custom: CustomTranslation) {
        customDao.insertCustomTranslation(custom)
    }

    suspend fun deleteCustomTranslationById(id: Int) {
        customDao.deleteCustomTranslationById(id)
    }

    suspend fun checkAndPrepopulateDictionary() {
        if (dictionaryDao.getWordCount() == 0) {
            val list = mutableListOf<DictionaryWord>()
            
            // --- GREETINGS ---
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "hello", bengaliText = "নমস্কার", hindiText = "नमस्ते", spanishText = "hola"))
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "how are you", bengaliText = "আপনি কেমন আছেন", hindiText = "आप कैसे हैं", spanishText = "¿cómo estás"))
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "thank you", bengaliText = "ধন্যবাদ", hindiText = "धन्यवाद", spanishText = "gracias"))
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "good morning", bengaliText = "শুভ সকাল", hindiText = "शुभ प्रभात", spanishText = "buenos días"))
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "good night", bengaliText = "শুভ রাত্রি", hindiText = "शुभ रात्रि", spanishText = "buenas noches"))
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "goodby", bengaliText = "বিদায়", hindiText = "अलविदा", spanishText = "adiós"))
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "please", bengaliText = "দয়া করে", hindiText = "कृपया", spanishText = "por favor"))
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "welcome", bengaliText = "স্বাগতম", hindiText = "स्वागत", spanishText = "bienvenido"))
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "excuse me", bengaliText = "শুনুন", hindiText = "माफ़ कीजिये", spanishText = "disculpe"))
            list.add(DictionaryWord(category = "✨ Greetings", englishText = "congratulations", bengaliText = "অভিনন্দন", hindiText = "बधाई हो", spanishText = "felicitaciones"))

            // --- TRAVEL & DIRECTIONS ---
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "where is the hotel", bengaliText = "হোটেলটি কোথায়", hindiText = "होटल कहाँ है", spanishText = "¿dónde está el hotel"))
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "where is the bathroom", bengaliText = "বাথরুম কোথায়", hindiText = "शौचालय कहाँ है", spanishText = "¿dónde está el baño"))
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "where is the airport", bengaliText = "বিমানবন্দর কোথায়", hindiText = "हवाई अड्डा कहाँ है", spanishText = "¿dónde está el aeropuerto"))
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "where is the train station", bengaliText = "রেল স্টেশন কোথায়", hindiText = "रेलवे स्टेशन कहाँ है", spanishText = "¿dónde está la estación de tren"))
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "how much is this ticket", bengaliText = "এই টিকিটের দাম কত", hindiText = "इस टिकट की कीमत कितनी है", spanishText = "¿cuánto cuesta este boleto"))
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "stop here", bengaliText = "এখানে থামুন", hindiText = "यहाँ रुकिए", spanishText = "deténgase aquí"))
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "turn left", bengaliText = "বাঁ দিকে ঘুরুন", hindiText = "बाएं मुड़ें", spanishText = "gira a la izquierda"))
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "turn right", bengaliText = "ডান দিকে ঘুরুন", hindiText = "दाएं मुड़ें", spanishText = "gira a la derecha"))
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "go straight", bengaliText = "সোজা যান", hindiText = "सीधे जाओ", spanishText = "ve derecho"))
            list.add(DictionaryWord(category = "🚇 Travel", englishText = "is it far", bengaliText = "এটা কি অনেক দূরে", hindiText = "क्या यह दूर है", spanishText = "¿está lejos"))

            // --- FOOD & DRINK ---
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "i am hungry", bengaliText = "আমি ক্ষুধার্ত", hindiText = "मुझे भूख लगी है", spanishText = "tengo hambre"))
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "i want water", bengaliText = "আমি জল চাই", hindiText = "मुझे पानी चाहिए", spanishText = "quiero agua"))
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "the food is delicious", bengaliText = "খাবারটি খুব সুস্বাদু", hindiText = "खाना बहुत स्वादिष्ट है", spanishText = "la comida está deliciosa"))
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "the bill please", bengaliText = "বিলটি দয়া করে দিন", hindiText = "कृप्या बिल लाइए", spanishText = "la cuenta por favor"))
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "where is a restaurant", bengaliText = "রেস্তোরাঁ কোথায়", hindiText = "एक रेस्तरां कहाँ है", spanishText = "¿dónde hay un restaurante"))
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "breakfast", bengaliText = "সকালের খাবার", hindiText = "सुबह का नाश्ता", spanishText = "desayuno"))
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "lunch", bengaliText = "দুপুরের খাবার", hindiText = "दोपहर का भोजन", spanishText = "almuerzo"))
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "dinner", bengaliText = "রাতের খাবার", hindiText = "रात का खाना", spanishText = "cena"))
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "tea", bengaliText = "চা", hindiText = "चाय", spanishText = "té"))
            list.add(DictionaryWord(category = "🍳 Food & Dining", englishText = "coffee", bengaliText = "কফি", hindiText = "कॉफ़ी", spanishText = "café"))

            // --- EMERGENCY ---
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "help me", bengaliText = "আমাকে সাহায্য করুন", hindiText = "मेरी मदद करो", spanishText = "ayúdame"))
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "call a doctor", bengaliText = "ডাক্তার ডাকুন", hindiText = "डॉक्टर को बुलाओ", spanishText = "llame a un médico"))
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "where is the hospital", bengaliText = "হাসপাতাল কোথায়", hindiText = "अस्पताल कहाँ है", spanishText = "¿dónde está el hospital"))
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "i am lost", bengaliText = "আমি হারিয়ে গেছি", hindiText = "मैं खो गया हूँ", spanishText = "estoy perdido"))
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "call the police", bengaliText = "পুলিশ ডাকুন", hindiText = "पुलिस को बुलाओ", spanishText = "llame a la policía"))
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "i need help", bengaliText = "আমার সাহায্য দরকার", hindiText = "मुझे मदद की ज़रूरत है", spanishText = "necesito ayuda"))
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "stop", bengaliText = "থামুন", hindiText = "रुकिए", spanishText = "alto"))
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "danger", bengaliText = "বিপদ", hindiText = "खतरा", spanishText = "peligro"))
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "fire", bengaliText = "আগুন", hindiText = "आग", spanishText = "fuego"))
            list.add(DictionaryWord(category = "🚨 Emergency", englishText = "medicine", bengaliText = "ওষুধ", hindiText = "दवा", spanishText = "medicina"))

            // --- COMMON NOUNS & OBJ ---
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "water", bengaliText = "জল", hindiText = "पानी", spanishText = "agua"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "friend", bengaliText = "বন্ধু", hindiText = "दोस्त", spanishText = "amigo"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "house", bengaliText = "বাড়ি", hindiText = "घर", spanishText = "casa"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "book", bengaliText = "বই", hindiText = "किताब", spanishText = "libro"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "phone", bengaliText = "ফোন", hindiText = "फ़ोन", spanishText = "teléfono"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "car", bengaliText = "গাড়ি", hindiText = "गाड़ी", spanishText = "coche"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "school", bengaliText = "বিদ্যালয়", hindiText = "विद्यालय", spanishText = "escuela"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "money", bengaliText = "টাকা", hindiText = "पैसा", spanishText = "dinero"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "time", bengaliText = "সময়", hindiText = "समय", spanishText = "tiempo"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "road", bengaliText = "রাস্তা", hindiText = "सड़क", spanishText = "camino"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "city", bengaliText = "শহর", hindiText = "शहर", spanishText = "ciudad"))
            list.add(DictionaryWord(category = "📘 Vocabulary: Nouns", englishText = "family", bengaliText = "পরিবার", hindiText = "परिवार", spanishText = "familia"))

            // --- VERBS ---
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "go", bengaliText = "যাওয়া", hindiText = "जाना", spanishText = "ir"))
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "come", bengaliText = "আসা", hindiText = "आना", spanishText = "venir"))
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "eat", bengaliText = "খাওয়া", hindiText = "खाना", spanishText = "comer"))
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "drink", bengaliText = "পান করা", hindiText = "पीना", spanishText = "beber"))
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "speak", bengaliText = "কথা বলা", hindiText = "बोलना", spanishText = "hablar"))
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "write", bengaliText = "লেখা", hindiText = "लिखना", spanishText = "escribir"))
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "read", bengaliText = "পড়া", hindiText = "पढ़ना", spanishText = "leer"))
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "run", bengaliText = "দৌড়ানো", hindiText = "दौड़ना", spanishText = "correr"))
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "see", bengaliText = "দেখা", hindiText = "देखना", spanishText = "ver"))
            list.add(DictionaryWord(category = "🏃 Vocabulary: Verbs", englishText = "sleep", bengaliText = "ঘুমানো", hindiText = "सोना", spanishText = "dormir"))

            // --- ADJECTIVES ---
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "good", bengaliText = "ভালো", hindiText = "अच्छा", spanishText = "bueno"))
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "bad", bengaliText = "খারাপ", hindiText = "बुरा", spanishText = "malo"))
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "happy", bengaliText = "খুশি", hindiText = "खुश", spanishText = "feliz"))
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "sad", bengaliText = "দুঃখিত", hindiText = "उदास", spanishText = "triste"))
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "beautiful", bengaliText = "সুন্দর", hindiText = "सुंदर", spanishText = "hermoso"))
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "big", bengaliText = "বড়", hindiText = "बड़ा", spanishText = "grande"))
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "small", bengaliText = "ছোট", hindiText = "छोटा", spanishText = "pequeño"))
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "hot", bengaliText = "গরম", hindiText = "गर्म", spanishText = "caliente"))
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "cold", bengaliText = "ঠান্ডা", hindiText = "ठंडा", spanishText = "frío"))
            list.add(DictionaryWord(category = "🎨 Vocabulary: Adjectives", englishText = "fast", bengaliText = "দ্রুত", hindiText = "तेज़", spanishText = "rápido"))

            dictionaryDao.insertWords(list)
        }
    }

    suspend fun getWordsByCategory(category: String): Flow<List<DictionaryWord>> {
        return dictionaryDao.getWordsByCategory(category)
    }

    suspend fun searchDatabase(query: String, langCode: String): List<DictionaryWord> {
        val all = dictionaryDao.getAllWords()
        val normalized = query.lowercase(Locale.ROOT).trim()
        if (normalized.isEmpty()) return emptyList()

        return all.filter {
            val value = when (langCode) {
                "en" -> it.englishText
                "bn" -> it.bengaliText
                "hi" -> it.hindiText
                "es" -> it.spanishText
                else -> ""
            }
            value.lowercase(Locale.ROOT).contains(normalized)
        }
    }

    suspend fun performOfflineTranslation(
        text: String,
        sourceCode: String,
        targetCode: String,
        customTranslationList: List<CustomTranslation> = emptyList()
    ): OfflineTranslationResult {
        if (text.isBlank()) return OfflineTranslationResult(text, isExactPhrase = false)
        if (sourceCode == targetCode) return OfflineTranslationResult(text, isExactPhrase = true)

        val cleanText = text.trim().lowercase(Locale.ROOT).replace(Regex("[.?!,¿¡]"), "")

        // 1. Check custom user-defined offline translation
        val customMatch = customTranslationList.firstOrNull {
            it.sourceLanguageCode == sourceCode &&
            it.targetLanguageCode == targetCode &&
            it.sourceText.trim().lowercase(Locale.ROOT).replace(Regex("[.?!,¿¡]"), "") == cleanText
        }
        if (customMatch != null) {
            return OfflineTranslationResult(
                translatedText = customMatch.translatedText,
                isExactPhrase = true,
                usedCustomTerm = true
            )
        }

        // 2. Fetch dictionary
        val allWords = dictionaryDao.getAllWords()

        // Form full exact match
        val phraseMatch = allWords.firstOrNull {
            val dbSourceText = getLanguageText(it, sourceCode).lowercase(Locale.ROOT).replace(Regex("[.?!,¿¡]"), "").trim()
            dbSourceText == cleanText
        }

        if (phraseMatch != null) {
            val transText = getLanguageText(phraseMatch, targetCode)
            return OfflineTranslationResult(
                translatedText = transText,
                isExactPhrase = true,
                matchedPhrase = phraseMatch
            )
        }

        // 3. Fallback to word-by-word tokenized translation
        val tokens = text.split(Regex("\\s+"))
        val translatedTokens = mutableListOf<String>()
        val breakdown = mutableListOf<WordTranslationDetail>()

        for (token in tokens) {
            val cleanToken = token.lowercase(Locale.ROOT).replace(Regex("[.?!,¿¡]"), "").trim()
            if (cleanToken.isEmpty()) continue

            // Find matching word
            val wordMatch = allWords.firstOrNull {
                val dbVal = getLanguageText(it, sourceCode).lowercase(Locale.ROOT).replace(Regex("[.?!,¿¡]"), "").trim()
                dbVal == cleanToken
            }

            if (wordMatch != null) {
                val transToken = getLanguageText(wordMatch, targetCode)
                translatedTokens.add(transToken)
                breakdown.add(
                    WordTranslationDetail(
                        originalWord = token,
                        translatedWord = transToken,
                        details = "Matched in offline dict: ${wordMatch.category}"
                    )
                )
            } else {
                // Keep the word as-is
                translatedTokens.add(token)
                breakdown.add(
                    WordTranslationDetail(
                        originalWord = token,
                        translatedWord = token,
                        details = "Not in offline dictionary"
                    )
                )
            }
        }

        val joinedText = translatedTokens.joinToString(" ")
        
        // 4. Find similar offline suggestions
        val suggestions = allWords.filter {
            val valInSource = getLanguageText(it, sourceCode).lowercase(Locale.ROOT)
            cleanText.split(" ").any { word -> word.length > 2 && valInSource.contains(word) }
        }.take(3)

        return OfflineTranslationResult(
            translatedText = joinedText,
            isExactPhrase = false,
            wordBreakdown = breakdown,
            phraseSuggestions = suggestions
        )
    }

    private fun getLanguageText(word: DictionaryWord, code: String): String {
        return when (code) {
            "en" -> word.englishText
            "bn" -> word.bengaliText
            "hi" -> word.hindiText
            "es" -> word.spanishText
            else -> word.englishText
        }
    }
}

data class OfflineTranslationResult(
    val translatedText: String,
    val isExactPhrase: Boolean,
    val usedCustomTerm: Boolean = false,
    val matchedPhrase: DictionaryWord? = null,
    val wordBreakdown: List<WordTranslationDetail> = emptyList(),
    val phraseSuggestions: List<DictionaryWord> = emptyList()
)

data class WordTranslationDetail(
    val originalWord: String,
    val translatedWord: String,
    val details: String
)
