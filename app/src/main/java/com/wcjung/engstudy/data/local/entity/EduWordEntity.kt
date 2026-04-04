package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "edu_words",
    indices = [
        Index("level"),
        Index("word")
    ]
)
data class EduWordEntity(
    @PrimaryKey val id: Int,
    val word: String,
    val meaning: String,
    val level: String,
    @ColumnInfo(name = "part_of_speech") val partOfSpeech: String = "",
    val variant1: String = "",
    val variant2: String = ""
)
