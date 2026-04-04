package com.wcjung.engstudy.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "known_items",
    indices = [Index(value = ["item_id", "item_type"], unique = true)]
)
data class KnownItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "item_type") val itemType: String,
    @ColumnInfo(name = "marked_at") val markedAt: Long = System.currentTimeMillis()
)
