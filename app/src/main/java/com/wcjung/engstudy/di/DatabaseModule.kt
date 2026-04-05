package com.wcjung.engstudy.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wcjung.engstudy.data.local.AppDatabase
import com.wcjung.engstudy.data.local.dao.BookmarkDao
import com.wcjung.engstudy.data.local.dao.EduWordDao
import com.wcjung.engstudy.data.local.dao.ExampleSentenceDao
import com.wcjung.engstudy.data.local.dao.IdiomDao
import com.wcjung.engstudy.data.local.dao.KnownItemDao
import com.wcjung.engstudy.data.local.dao.LearningProgressDao
import com.wcjung.engstudy.data.local.dao.WordDao
import com.wcjung.engstudy.data.local.dao.WrongAnswerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE learning_progress ADD COLUMN is_excluded INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS example_sentences (
                    id INTEGER PRIMARY KEY NOT NULL,
                    sentence_en TEXT NOT NULL,
                    sentence_ko TEXT NOT NULL,
                    grammar_topic TEXT NOT NULL DEFAULT 'general',
                    grammar_topic_ko TEXT NOT NULL DEFAULT '일반',
                    level TEXT NOT NULL DEFAULT '초급',
                    word_count INTEGER NOT NULL DEFAULT 0)"""
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS known_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    item_id INTEGER NOT NULL,
                    item_type TEXT NOT NULL,
                    marked_at INTEGER NOT NULL)"""
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_known_items_item_id_item_type ON known_items(item_id, item_type)")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS idioms (
                    id INTEGER PRIMARY KEY NOT NULL,
                    phrase TEXT NOT NULL,
                    meaning TEXT NOT NULL,
                    meaning_type TEXT NOT NULL DEFAULT 'en',
                    type TEXT NOT NULL DEFAULT 'idiom',
                    example_en TEXT NOT NULL DEFAULT '',
                    example_ko TEXT NOT NULL DEFAULT '',
                    difficulty INTEGER NOT NULL DEFAULT 3,
                    category TEXT NOT NULL DEFAULT 'daily')"""
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_idioms_type ON idioms(type)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_idioms_category ON idioms(category)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_idioms_phrase ON idioms(phrase)")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS wrong_answers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    word_id INTEGER NOT NULL,
                    wrong_answer TEXT NOT NULL,
                    correct_answer TEXT NOT NULL,
                    quiz_type TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE)"""
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_wrong_answers_word_id ON wrong_answers(word_id)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "engstudy.db"
        )
            .createFromAsset("databases/engstudy.db")
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            // TODO: Before release, write proper migrations for all versions and remove fallbackToDestructiveMigration.
            // Kept temporarily as a safety net during development — only triggers if no migration path exists.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideWordDao(database: AppDatabase): WordDao = database.wordDao()

    @Provides
    fun provideLearningProgressDao(database: AppDatabase): LearningProgressDao =
        database.learningProgressDao()

    @Provides
    fun provideBookmarkDao(database: AppDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideEduWordDao(database: AppDatabase): EduWordDao = database.eduWordDao()

    @Provides
    fun provideWrongAnswerDao(database: AppDatabase): WrongAnswerDao = database.wrongAnswerDao()

    @Provides
    fun provideIdiomDao(database: AppDatabase): IdiomDao = database.idiomDao()

    @Provides
    fun provideSentenceDao(database: AppDatabase): ExampleSentenceDao = database.sentenceDao()

    @Provides
    fun provideKnownItemDao(database: AppDatabase): KnownItemDao = database.knownItemDao()
}
