package com.example.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.CustomTranslation
import com.example.data.model.DictionaryWord
import com.example.data.model.LanguagePack
import com.example.data.model.TranslationHistory
import com.example.data.repository.OfflineTranslationResult
import com.example.data.repository.TranslationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class TranslationViewModel(
    application: Application,
    private val repository: TranslationRepository
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val context = application.applicationContext

    // App Initialization and Splash
    private val _isSplashLoaded = MutableStateFlow(false)
    val isSplashLoaded: StateFlow<Boolean> = _isSplashLoaded.asStateFlow()

    // Workspace Active Tab
    private val _activeTab = MutableStateFlow(0) // 0 = Translate, 1 = Dictionary, 2 = History
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Core Translate States
    private val _sourceText = MutableStateFlow("")
    val sourceText: StateFlow<String> = _sourceText.asStateFlow()

    private val _sourceLangCode = MutableStateFlow("en")
    val sourceLangCode: StateFlow<String> = _sourceLangCode.asStateFlow()

    private val _targetLangCode = MutableStateFlow("bn")
    val targetLangCode: StateFlow<String> = _targetLangCode.asStateFlow()

    private val _translationResult = MutableStateFlow<OfflineTranslationResult?>(null)
    val translationResult: StateFlow<OfflineTranslationResult?> = _translationResult.asStateFlow()

    // In-App Toast heads-up alert system
    private val _inAppNotification = MutableStateFlow<String?>(null)
    val inAppNotification: StateFlow<String?> = _inAppNotification.asStateFlow()

    // Custom alerts counter
    private val _activeNotificationCount = MutableStateFlow(0)
    val activeNotificationCount: StateFlow<Int> = _activeNotificationCount.asStateFlow()

    // Dictionary Explorer states
    private val _dictSearchQuery = MutableStateFlow("")
    val dictSearchQuery: StateFlow<String> = _dictSearchQuery.asStateFlow()

    private val _dictSelectedCategory = MutableStateFlow("✨ Greetings")
    val dictSelectedCategory: StateFlow<String> = _dictSelectedCategory.asStateFlow()

    private val _dictSearchResults = MutableStateFlow<List<DictionaryWord>>(emptyList())
    val dictSearchResults: StateFlow<List<DictionaryWord>> = _dictSearchResults.asStateFlow()

    // Custom dictionary additions
    private val _customSourceWord = MutableStateFlow("")
    val customSourceWord: StateFlow<String> = _customSourceWord.asStateFlow()

    private val _customTargetWord = MutableStateFlow("")
    val customTargetWord: StateFlow<String> = _customTargetWord.asStateFlow()

    // TextToSpeech State
    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    // DB Flows
    val historyList: StateFlow<List<TranslationHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarksList: StateFlow<List<TranslationHistory>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val languagePacksList: StateFlow<List<LanguagePack>> = repository.allPacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customTranslationsList: StateFlow<List<CustomTranslation>> = repository.customTranslations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Debounce Translation Job
    private var translateJob: Job? = null

    init {
        // Create Notification Channel for Android 8.0+
        createNotificationChannel()

        // Initialize Text To Speech
        tts = TextToSpeech(context, this)

        // Prep database and default items with offline datasets
        viewModelScope.launch {
            repository.initializeLanguagePacks()
            repository.checkAndPrepopulateDictionary()
            
            // Splash loading animation delay (3 seconds of lovely scrolling greetings)
            delay(3000)
            _isSplashLoaded.value = true
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isTtsReady.value = true
        }
    }

    fun speak(text: String, langCode: String) {
        val ttsEngine = tts ?: return
        if (!_isTtsReady.value) {
            showNotification("Speech synthesis initializing, please try again.")
            return
        }

        viewModelScope.launch {
            val locale = when (langCode) {
                "en" -> Locale.ENGLISH
                "es" -> Locale("es", "ES")
                "hi" -> Locale("hi", "IN")
                "bn" -> Locale("bn", "BD")
                else -> Locale.ENGLISH
            }

            val result = ttsEngine.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallBackResult = ttsEngine.setLanguage(Locale.ENGLISH)
                if (fallBackResult != TextToSpeech.LANG_MISSING_DATA && fallBackResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TranslationTTS")
                    showNotification("Preferred voice pack unavailable offline. Using English synthesis.")
                } else {
                    showNotification("Speech engine synthesis is currently unavailable.")
                }
            } else {
                ttsEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TranslationTTS")
            }
        }
    }

    // Tab control
    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    // Input text changed
    fun onSourceTextChanged(text: String) {
        _sourceText.value = text
        
        // Debounce translations
        translateJob?.cancel()
        translateJob = viewModelScope.launch {
            delay(300) // 300ms delay to avoid massive typing lag
            performTranslation()
        }
    }

    // Language Codes Updates
    fun setSourceLanguage(code: String) {
        _sourceLangCode.value = code
        // Swap if same
        if (_targetLangCode.value == code) {
            _targetLangCode.value = if (code == "en") "bn" else "en"
        }
        performTranslation()
    }

    fun setTargetLanguage(code: String) {
        _targetLangCode.value = code
        // Swap if same
        if (_sourceLangCode.value == code) {
            _sourceLangCode.value = if (code == "en") "bn" else "en"
        }
        performTranslation()
    }

    fun swapLanguages() {
        val src = _sourceLangCode.value
        val tgt = _targetLangCode.value
        _sourceLangCode.value = tgt
        _targetLangCode.value = src

        val currentSrcText = _sourceText.value
        val currentTgtText = _translationResult.value?.translatedText ?: ""
        
        _sourceText.value = currentTgtText
        performTranslation()
        showNotification("Languages Swapped: ${src.uppercase()} ⇄ ${tgt.uppercase()}")
    }

    // Translation Main Algorithm
    private fun performTranslation() {
        viewModelScope.launch {
            val text = _sourceText.value
            if (text.isBlank()) {
                _translationResult.value = null
                return@launch
            }

            // Check if Language Pack is downloaded offline first!
            val targetPack = repository.getPackByCode(_targetLangCode.value)
            val sourcePack = repository.getPackByCode(_sourceLangCode.value)

            val targetDownloaded = targetPack?.isDownloaded ?: true
            val sourceDownloaded = sourcePack?.isDownloaded ?: true

            if (!targetDownloaded || !sourceDownloaded) {
                // If either is the base (English) it doesn't need to be downloaded
                val pendingCode = if (!sourceDownloaded) _sourceLangCode.value else _targetLangCode.value
                val langName = when(pendingCode) {
                    "bn" -> "Bengali"
                    "hi" -> "Hindi"
                    "es" -> "Spanish"
                    else -> "Target Language"
                }
                
                _translationResult.value = OfflineTranslationResult(
                    translatedText = "[Offline Pack Missing] Download the $langName language pack to unlock offline support.",
                    isExactPhrase = false
                )
                return@launch
            }

            val result = repository.performOfflineTranslation(
                text = text,
                sourceCode = _sourceLangCode.value,
                targetCode = _targetLangCode.value,
                customTranslationList = customTranslationsList.value
            )

            _translationResult.value = result

            // Auto-Save into history (if it's not a missing pack warning, and is real)
            saveToHistoryDelayed(text, result.translatedText)
        }
    }

    private var historySaveJob: Job? = null
    private fun saveToHistoryDelayed(src: String, tgt: String) {
        historySaveJob?.cancel()
        historySaveJob = viewModelScope.launch {
            delay(1500) // Don't spam database on every keypress, wait 1.5s after typing halts
            if (src.isNotBlank() && tgt.isNotBlank() && !tgt.startsWith("[Offline Pack Missing]")) {
                repository.insertHistory(
                    TranslationHistory(
                        sourceLanguageCode = _sourceLangCode.value,
                        targetLanguageCode = _targetLangCode.value,
                        sourceText = src,
                        translatedText = tgt
                    )
                )
            }
        }
    }

    // Toggle Bookmarks
    fun toggleBookmark(history: TranslationHistory) {
        viewModelScope.launch {
            val updated = history.copy(isBookmarked = !history.isBookmarked)
            repository.updateHistory(updated)
            val message = if (updated.isBookmarked) "Added to Bookmarks!" else "Removed from Bookmarks"
            showNotification(message)
        }
    }

    fun deleteHistoryItem(history: TranslationHistory) {
        viewModelScope.launch {
            repository.deleteHistory(history)
            showNotification("Translation removed from history")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showNotification("Translation history cleared.")
            triggerSystemNotification("History Log Purged", "All translation caches have been successfully erased.")
        }
    }

    // Custom Translation Dictionary Manager
    fun onCustomSourceWordChanged(word: String) { _customSourceWord.value = word }
    fun onCustomTargetWordChanged(word: String) { _customTargetWord.value = word }

    fun addNewCustomTranslation() {
        val src = _customSourceWord.value.trim()
        val tgt = _customTargetWord.value.trim()
        if (src.isEmpty() || tgt.isEmpty()) {
            showNotification("Fields cannot be empty.")
            return
        }

        viewModelScope.launch {
            repository.insertCustomTranslation(
                CustomTranslation(
                    sourceLanguageCode = _sourceLangCode.value,
                    targetLanguageCode = _targetLangCode.value,
                    sourceText = src,
                    translatedText = tgt
                )
            )
            _customSourceWord.value = ""
            _customTargetWord.value = ""
            showNotification("Successfully added custom offline translation!")
            performTranslation() // Refresh
        }
    }

    fun deleteCustomTranslation(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomTranslationById(id)
            showNotification("Custom offline phrase removed")
            performTranslation() // Refresh
        }
    }

    // Dictionary Searches
    fun onDictSearchQueryChanged(q: String) {
        _dictSearchQuery.value = q
        performDictSearch()
    }

    fun setDictCategory(cat: String) {
        _dictSelectedCategory.value = cat
        _dictSearchQuery.value = ""
        _dictSearchResults.value = emptyList()
    }

    private fun performDictSearch() {
        viewModelScope.launch {
            val q = _dictSearchQuery.value
            if (q.isBlank()) {
                _dictSearchResults.value = emptyList()
                return@launch
            }
            // Search in specified language
            val matched = repository.searchDatabase(q, _sourceLangCode.value)
            _dictSearchResults.value = matched
        }
    }

    // Language Pack Simulated Download (with awesome notifications & animations)
    fun downloadLanguagePack(pack: LanguagePack) {
        if (pack.isDownloaded || pack.isDownloading) return

        viewModelScope.launch {
            // Update UI downloading status
            repository.updatePack(pack.copy(isDownloading = true, progress = 0.0f))
            showNotification("Downloading ${pack.languageName} Offline Pack...")

            var progressCount = 0f
            while (progressCount < 1.0f) {
                delay(120) // Increment over a lovely smooth timeline
                progressCount += 0.05f
                if (progressCount > 1.0f) progressCount = 1.0f
                repository.updatePack(pack.copy(isDownloading = true, progress = progressCount))
            }

            // Successfully downloaded
            repository.updatePack(pack.copy(isDownloading = false, isDownloaded = true, progress = 1.0f))
            
            val message = "${pack.languageName} Offline Language Pack successfully downloaded (${pack.sizeMB} MB)"
            showNotification(message)
            
            // Trigger actual system notification
            triggerSystemNotification(
                title = "Language Model Installed 🛜",
                content = "${pack.languageName} pack is now downloaded and fully operational offline. Translation accuracy increased."
            )

            // Re-perform translation in case the user has text loaded!
            performTranslation()
        }
    }

    fun removeLanguagePack(pack: LanguagePack) {
        if (!pack.isDownloaded || pack.languageCode == "en") return
        viewModelScope.launch {
            repository.updatePack(pack.copy(isDownloaded = false, progress = 0.0f, isDownloading = false))
            showNotification("Uninstalled ${pack.languageName} Pack")
            triggerSystemNotification(
                title = "Language Pack Removed",
                content = "${pack.languageName} offline model was uninstalled. Internet is now required to search this catalog."
            )
            performTranslation() // Re-translate which will trigger download hint
        }
    }

    // Notification Alerts systems
    private fun showNotification(message: String) {
        _inAppNotification.value = message
        _activeNotificationCount.value += 1
        
        viewModelScope.launch {
            delay(2800) // Clear in-app banner after 2.8s
            if (_inAppNotification.value == message) {
                _inAppNotification.value = null
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Offline Translator channel"
            val descriptionText = "Notifications for Offline Translation completed downloads"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("offline_translator_channel", name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun triggerSystemNotification(title: String, content: String) {
        val builder = NotificationCompat.Builder(context, "offline_translator_channel")
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // system standard check
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            // Android 13 POST_NOTIFICATIONS permission not granted, silent fallback
        }
    }

    override fun onCleared() {
        tts?.shutdown()
        super.onCleared()
    }
}
