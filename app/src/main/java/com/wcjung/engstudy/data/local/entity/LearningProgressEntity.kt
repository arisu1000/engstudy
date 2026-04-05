package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_progress",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("word_id", unique = true),
        Index("next_review_date")
    ]
)
data class LearningProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "word_id") val wordId: Int,
    @ColumnInfo(name = "ease_factor") val easeFactor: Float = 2.5f,
    @ColumnInfo(name = "interval_days") val intervalDays: Int = 0,
    val repetitions: Int = 0,
    @ColumnInfo(name = "next_review_date") val nextReviewDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_reviewed_date") val lastReviewedDate: Long? = null,
    @ColumnInfo(name = "times_correct") val timesCorrect: Int = 0,
    @ColumnInfo(name = "times_incorrect") val timesIncorrect: Int = 0,
    @ColumnInfo(name = "is_learned") val isLearned: Boolean = false,
    @ColumnInfo(name = "is_excluded") val isExcluded: Boolean = false
)
