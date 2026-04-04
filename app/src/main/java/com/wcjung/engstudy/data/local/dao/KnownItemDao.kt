package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wcjung.engstudy.data.local.entity.KnownItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnownItemDao {

    @Query("SELECT EXISTS(SELECT 1 FROM known_items WHERE item_id = :itemId AND item_type = :itemType)")
    fun isKnown(itemId: Int, itemType: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markAsKnown(entity: KnownItemEntity)

    @Query("DELETE FROM known_items WHERE item_id = :itemId AND item_type = :itemType")
    suspend fun unmarkKnown(itemId: Int, itemType: String)

    @Query("SELECT item_id FROM known_items WHERE item_type = :itemType")
    fun getKnownIds(itemType: String): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM known_items WHERE item_type = :itemType")
    fun getKnownCount(itemType: String): Flow<Int>
}
