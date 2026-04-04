package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "wrong_answers", indices = [Index("word_id")])
data class WrongAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "word_id") val wordId: Int,
    @ColumnInfo(name = "wrong_answer") val wrongAnswer: String,
    @ColumnInfo(name = "correct_answer") val correctAnswer: String,
    @ColumnInfo(name = "quiz_type") val quizType: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
