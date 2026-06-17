package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceLanguageCode: String,
    val targetLanguageCode: String,
    val sourceText: String,
    val translatedText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)

@Entity(tableName = "dictionary_words")
data class DictionaryWord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val englishText: String,
    val bengaliText: String,
    val hindiText: String,
    val spanishText: String
)

@Entity(tableName = "custom_translations")
data class CustomTranslation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceLanguageCode: String,
    val targetLanguageCode: String,
    val sourceText: String,
    val translatedText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "language_packs")
data class LanguagePack(
    @PrimaryKey val languageCode: String,
    val languageName: String,
    val isDownloaded: Boolean,
    val sizeMB: Float,
    val progress: Float = 0f,
    val isDownloading: Boolean = false
)
