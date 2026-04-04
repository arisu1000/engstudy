package com.wcjung.engstudy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wcjung.engstudy.data.local.dao.BookmarkDao
import com.wcjung.engstudy.data.local.dao.EduWordDao
import com.wcjung.engstudy.data.local.dao.ExampleSentenceDao
import com.wcjung.engstudy.data.local.dao.KnownItemDao
import com.wcjung.engstudy.data.local.dao.LearningProgressDao
import com.wcjung.engstudy.data.local.dao.WordDao
import com.wcjung.engstudy.data.local.dao.IdiomDao
import com.wcjung.engstudy.data.local.dao.WrongAnswerDao
import com.wcjung.engstudy.data.local.entity.BookmarkEntity
import com.wcjung.engstudy.data.local.entity.EduWordEntity
import com.wcjung.engstudy.data.local.entity.ExampleSentenceEntity
import com.wcjung.engstudy.data.local.entity.IdiomEntity
import com.wcjung.engstudy.data.local.entity.KnownItemEntity
import com.wcjung.engstudy.data.local.entity.LearningProgressEntity
import com.wcjung.engstudy.data.local.entity.WordEntity
import com.wcjung.engstudy.data.local.entity.WrongAnswerEntity

@Database(
    entities = [
        WordEntity::class,
        LearningProgressEntity::class,
        BookmarkEntity::class,
        EduWordEntity::class,
        WrongAnswerEntity::class,
        IdiomEntity::class,
        ExampleSentenceEntity::class,
        KnownItemEntity::class
    ],
    version = 7,
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
}
