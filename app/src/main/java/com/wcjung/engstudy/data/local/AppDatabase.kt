package com.wcjung.engstudy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wcjung.engstudy.data.local.dao.BackupDao
import com.wcjung.engstudy.data.local.dao.BookmarkDao
import com.wcjung.engstudy.data.local.dao.EduBookmarkDao
import com.wcjung.engstudy.data.local.dao.EduExcludedWordDao
import com.wcjung.engstudy.data.local.dao.EduWordDao
import com.wcjung.engstudy.data.local.dao.ExampleSentenceDao
import com.wcjung.engstudy.data.local.dao.KnownItemDao
import com.wcjung.engstudy.data.local.dao.LearningProgressDao
import com.wcjung.engstudy.data.local.dao.WordDao
import com.wcjung.engstudy.data.local.dao.IdiomDao
import com.wcjung.engstudy.data.local.dao.WrongAnswerDao
import com.wcjung.engstudy.data.local.dao.UserWordDao
import com.wcjung.engstudy.data.local.dao.UserWordMeaningDao
import com.wcjung.engstudy.data.local.dao.WordMeaningDao
import com.wcjung.engstudy.data.local.dao.WordExampleDao
import com.wcjung.engstudy.data.local.entity.BookmarkEntity
import com.wcjung.engstudy.data.local.entity.EduBookmarkEntity
import com.wcjung.engstudy.data.local.entity.EduExcludedWordEntity
import com.wcjung.engstudy.data.local.entity.EduWordEntity
import com.wcjung.engstudy.data.local.entity.ExampleSentenceEntity
import com.wcjung.engstudy.data.local.entity.IdiomEntity
import com.wcjung.engstudy.data.local.entity.KnownItemEntity
import com.wcjung.engstudy.data.local.entity.LearningProgressEntity
import com.wcjung.engstudy.data.local.entity.WordEntity
import com.wcjung.engstudy.data.local.entity.WrongAnswerEntity
import com.wcjung.engstudy.data.local.entity.UserWordMeaningEntity
import com.wcjung.engstudy.data.local.entity.WordMeaningEntity
import com.wcjung.engstudy.data.local.entity.WordExampleEntity

@Database(
    entities = [
        WordEntity::class,
        LearningProgressEntity::class,
        BookmarkEntity::class,
        EduWordEntity::class,
        WrongAnswerEntity::class,
        IdiomEntity::class,
        ExampleSentenceEntity::class,
        KnownItemEntity::class,
        WordMeaningEntity::class,
        WordExampleEntity::class,
        UserWordMeaningEntity::class,
        EduBookmarkEntity::class,
        EduExcludedWordEntity::class
    ],
    version = 12,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun learningProgressDao(): LearningProgressDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun eduWordDao(): EduWordDao
    abstract fun wrongAnswerDao(): WrongAnswerDao
    abstract fun idiomDao(): IdiomDao
    abstract fun sentenceDao(): ExampleSentenceDao
    abstract fun knownItemDao(): KnownItemDao
    abstract fun wordMeaningDao(): WordMeaningDao
    abstract fun wordExampleDao(): WordExampleDao
    abstract fun userWordMeaningDao(): UserWordMeaningDao
    abstract fun userWordDao(): UserWordDao
    abstract fun backupDao(): BackupDao
    abstract fun eduBookmarkDao(): EduBookmarkDao
    abstract fun eduExcludedWordDao(): EduExcludedWordDao
}
