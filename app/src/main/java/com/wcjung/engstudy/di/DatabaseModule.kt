package com.wcjung.engstudy.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wcjung.engstudy.data.local.AppDatabase
import com.wcjung.engstudy.data.local.dao.BookmarkDao
import com.wcjung.engstudy.data.local.dao.EduWordDao
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
            .addMigrations(MIGRATION_4_5)
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
}
