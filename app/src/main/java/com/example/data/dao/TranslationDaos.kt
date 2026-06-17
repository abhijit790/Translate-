package com.example.data.dao

import androidx.room.*
import com.example.data.model.CustomTranslation
import com.example.data.model.DictionaryWord
import com.example.data.model.LanguagePack
import com.example.data.model.TranslationHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationHistoryDao {
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TranslationHistory>>

    @Query("SELECT * FROM translation_history WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarks(): Flow<List<TranslationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: TranslationHistory)

    @Update
    suspend fun updateHistory(history: TranslationHistory)

    @Delete
    suspend fun deleteHistory(history: TranslationHistory)

    @Query("DELETE FROM translation_history")
    suspend fun clearAllHistory()
}

@Dao
interface DictionaryWordDao {
    @Query("SELECT * FROM dictionary_words")
    suspend fun getAllWords(): List<DictionaryWord>

    @Query("SELECT * FROM dictionary_words WHERE category = :category")
    fun getWordsByCategory(category: String): Flow<List<DictionaryWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<DictionaryWord>)

    @Query("SELECT COUNT(*) FROM dictionary_words")
    suspend fun getWordCount(): Int
}

@Dao
interface CustomTranslationDao {
    @Query("SELECT * FROM custom_translations ORDER BY timestamp DESC")
    fun getAllCustomTranslations(): Flow<List<CustomTranslation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTranslation(custom: CustomTranslation)

    @Query("DELETE FROM custom_translations WHERE id = :id")
    suspend fun deleteCustomTranslationById(id: Int)
}

@Dao
interface LanguagePackDao {
    @Query("SELECT * FROM language_packs")
    fun getAllPacks(): Flow<List<LanguagePack>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPacks(packs: List<LanguagePack>)

    @Update
    suspend fun updatePack(pack: LanguagePack)

    @Query("SELECT * FROM language_packs WHERE languageCode = :code")
    suspend fun getPackByCode(code: String): LanguagePack?
}
