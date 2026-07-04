package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 사용자가 직접 추가/변경한 단어 의미 (v11 추가).
 *
 * words/word_meanings는 콘텐츠 테이블이라 앱 업데이트(콘텐츠 마이그레이션) 때
 * 통째로 교체된다. 사용자 편집본은 이 테이블에 분리 저장해야 유실되지 않는다.
 *
 * - is_primary = true: 대표 뜻을 이 값으로 교체 (단어당 최대 1개, DAO에서 보장)
 * - is_primary = false: 목록에 추가로 표시되는 "내 의미"
 */
@Entity(
    tableName = "user_word_meanings",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("word_id")]
)
data class UserWordMeaningEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "word_id") val wordId: Int,
    val meaning: String,
    @ColumnInfo(name = "is_primary") val isPrimary: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
