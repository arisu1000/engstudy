package com.wcjung.engstudy.di

import com.wcjung.engstudy.data.repository.BookmarkRepositoryImpl
import com.wcjung.engstudy.data.repository.LearningRepositoryImpl
import com.wcjung.engstudy.data.repository.WordRepositoryImpl
import com.wcjung.engstudy.domain.repository.BookmarkRepository
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWordRepository(impl: WordRepositoryImpl): WordRepository

    @Binds
    @Singleton
    abstract fun bindLearningRepository(impl: LearningRepositoryImpl): LearningRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository
}
