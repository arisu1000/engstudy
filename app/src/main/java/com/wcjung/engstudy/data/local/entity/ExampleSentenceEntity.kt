package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "example_sentences")
data class ExampleSentenceEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "sentence_en") val sentenceEn: String,
    @ColumnInfo(name = "sentence_ko") val sentenceKo: String,
    @ColumnInfo(name = "grammar_topic") val grammarTopic: String = "general",
    @ColumnInfo(name = "grammar_topic_ko") val grammarTopicKo: String = "일반",
    val level: String = "초급",
    @ColumnInfo(name = "word_count") val wordCount: Int = 0
)
