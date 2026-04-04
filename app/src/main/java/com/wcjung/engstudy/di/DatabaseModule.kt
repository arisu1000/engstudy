package com.wcjung.engstudy.di

import android.content.Context
import androidx.room.Room
import com.wcjung.engstudy.data.local.AppDatabase
import com.wcjung.engstudy.data.local.dao.BookmarkDao
import com.wcjung.engstudy.data.local.dao.LearningProgressDao
import com.wcjung.engstudy.data.local.dao.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "engstudy.db"
        )
            .createFromAsset("databases/engstudy.db")
            .build()
    }

    @Provides
    fun provideWordDao(database: AppDatabase): WordDao = database.wordDao()

    @Provides
    fun provideLearningProgressDao(database: AppDatabase): LearningProgressDao =
        database.learningProgressDao()

    @Provides
    fun provideBookmarkDao(database: AppDatabase): BookmarkDao = database.bookmarkDao()
}
