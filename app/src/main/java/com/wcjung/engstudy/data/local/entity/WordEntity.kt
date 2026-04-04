package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    indices = [
        Index("domain"),
        Index("age_group"),
        Index("frequency_rank"),
        Index("word")
    ]
)
data class WordEntity(
    @PrimaryKey val id: Int,
    val word: String,
    val pronunciation: String,
    @ColumnInfo(name = "meaning_ko") val meaningKo: String,
    @ColumnInfo(name = "part_of_speech") val partOfSpeech: String,
    @ColumnInfo(name = "example_en") val exampleEn: String,
    @ColumnInfo(name = "example_ko") val exampleKo: String,
    val domain: String,
    @ColumnInfo(name = "age_group") val ageGroup: String,
    @ColumnInfo(name = "frequency_rank") val frequencyRank: Int,
    val difficulty: Int,
    val synonyms: String? = null,
    val antonyms: String? = null,
    val notes: String? = null
)
