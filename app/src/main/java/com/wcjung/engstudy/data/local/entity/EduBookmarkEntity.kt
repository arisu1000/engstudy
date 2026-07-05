package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "edu_bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = EduWordEntity::class,
            parentColumns = ["id"],
            childColumns = ["edu_word_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("edu_word_id", unique = true)]
)
data class EduBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "edu_word_id") val eduWordId: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
