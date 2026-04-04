package com.wcjung.engstudy.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.wcjung.engstudy.data.local.entity.IdiomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdiomDao {

    @Query("SELECT * FROM idioms ORDER BY id ASC")
    fun getAllIdioms(): Flow<List<IdiomEntity>>

    @Query("SELECT * FROM idioms WHERE type = :type ORDER BY id ASC")
    fun getByType(type: String): Flow<List<IdiomEntity>>

    @Query(
        """
        SELECT * FROM idioms
        WHERE phrase LIKE '%' || :query || '%'
        OR meaning LIKE '%' || :query || '%'
        ORDER BY id ASC
        LIMIT 50
        """
    )
    fun searchIdioms(query: String): Flow<List<IdiomEntity>>

    @Query(
        """
        SELECT * FROM idioms
        WHERE id != :excludeId
        ORDER BY RANDOM() LIMIT :count
        """
    )
    suspend fun getRandomIdioms(excludeId: Int, count: Int = 3): List<IdiomEntity>

    @Query("SELECT COUNT(*) FROM idioms")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM idioms WHERE type = :type")
    fun getCountByType(type: String): Flow<Int>

    @Query("SELECT * FROM idioms WHERE id = :id")
    suspend fun getIdiomById(id: Int): IdiomEntity?
}
