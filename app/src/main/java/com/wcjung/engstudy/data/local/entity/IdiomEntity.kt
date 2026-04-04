package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "idioms",
    indices = [
        Index("type"),
        Index("category"),
        Index("phrase")
    ]
)
data class IdiomEntity(
    @PrimaryKey val id: Int,
    val phrase: String,
    val meaning: String,
    @ColumnInfo(name = "meaning_type") val meaningType: String = "en",
    val type: String = "idiom",
    @ColumnInfo(name = "example_en") val exampleEn: String = "",
    @ColumnInfo(name = "example_ko") val exampleKo: String = "",
    val difficulty: Int = 3,
    val category: String = "daily"
)
