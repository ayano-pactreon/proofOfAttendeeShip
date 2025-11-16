package com.feooh.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for event operations
 */
@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("SELECT * FROM events WHERE event_id = :eventId")
    suspend fun getEvent(eventId: String): EventEntity?

    @Query("SELECT * FROM events ORDER BY last_synced DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("DELETE FROM events WHERE event_id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
