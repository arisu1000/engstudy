package com.wcjung.engstudy.di

import com.wcjung.engstudy.data.repository.BookmarkRepositoryImpl
import com.wcjung.engstudy.data.repository.EduWordRepositoryImpl
import com.wcjung.engstudy.data.repository.IdiomRepositoryImpl
import com.wcjung.engstudy.data.repository.LearningRepositoryImpl
import com.wcjung.engstudy.data.repository.SentenceRepositoryImpl
import com.wcjung.engstudy.data.repository.WordRepositoryImpl
import com.wcjung.engstudy.data.repository.WrongAnswerRepositoryImpl
import com.wcjung.engstudy.domain.repository.BookmarkRepository
import com.wcjung.engstudy.domain.repository.EduWordRepository
import com.wcjung.engstudy.domain.repository.IdiomRepository
import com.wcjung.engstudy.domain.repository.LearningRepository
import com.wcjung.engstudy.domain.repository.SentenceRepository
import com.wcjung.engstudy.domain.repository.WordRepository
import com.wcjung.engstudy.domain.repository.WrongAnswerRepository
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

    @Binds
    @Singleton
    abstract fun bindEduWordRepository(impl: EduWordRepositoryImpl): EduWordRepository

    @Binds
    @Singleton
    abstract fun bindWrongAnswerRepository(impl: WrongAnswerRepositoryImpl): WrongAnswerRepository

    @Binds
    @Singleton
    abstract fun bindIdiomRepository(impl: IdiomRepositoryImpl): IdiomRepository

    @Binds
    @Singleton
    abstract fun bindSentenceRepository(impl: SentenceRepositoryImpl): SentenceRepository
}
