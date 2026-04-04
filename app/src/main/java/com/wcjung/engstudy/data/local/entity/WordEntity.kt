package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    indices = [
        Index("stage"),
        Index("domain"),
        Index("frequency_rank"),
        Index("word")
    ]
)
data class WordEntity(
    @PrimaryKey val id: Int,
    val word: String,
    val pronunciation: String = "",
    val meaning: String,
    @ColumnInfo(name = "meaning_type") val meaningType: String = "ko",
    @ColumnInfo(name = "part_of_speech") val partOfSpeech: String = "noun",
    @ColumnInfo(name = "example_en") val exampleEn: String = "",
    @ColumnInfo(name = "example_ko") val exampleKo: String = "",
    val stage: Int,
    val domain: String = "GENERAL",
    @ColumnInfo(name = "frequency_rank") val frequencyRank: Int,
    val difficulty: Int = 3,
    val synonyms: String? = null,
    val antonyms: String? = null,
    val notes: String? = null
)
