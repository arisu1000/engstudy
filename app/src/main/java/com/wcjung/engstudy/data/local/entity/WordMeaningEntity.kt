package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "word_meanings",
    indices = [Index("word_id")],
    foreignKeys = [ForeignKey(
        entity = WordEntity::class,
        parentColumns = ["id"],
        childColumns = ["word_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class WordMeaningEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "word_id") val wordId: Int,
    val meaning: String,
    val pos: String = "noun",
    @ColumnInfo(name = "meaning_type") val meaningType: String = "ko",
    @ColumnInfo(name = "sense_order") val senseOrder: Int = 0,
    val source: String = "kengdic"
)
