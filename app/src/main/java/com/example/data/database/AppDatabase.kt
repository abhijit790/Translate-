package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CustomTranslationDao
import com.example.data.dao.DictionaryWordDao
import com.example.data.dao.LanguagePackDao
import com.example.data.dao.TranslationHistoryDao
import com.example.data.model.CustomTranslation
import com.example.data.model.DictionaryWord
import com.example.data.model.LanguagePack
import com.example.data.model.TranslationHistory

@Database(
    entities = [
        TranslationHistory::class,
        DictionaryWord::class,
        CustomTranslation::class,
        LanguagePack::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun translationHistoryDao(): TranslationHistoryDao
    abstract fun dictionaryWordDao(): DictionaryWordDao
    abstract fun customTranslationDao(): CustomTranslationDao
    abstract fun languagePackDao(): LanguagePackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offline_translator_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
