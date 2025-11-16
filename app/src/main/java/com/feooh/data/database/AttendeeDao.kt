package com.feooh.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for attendee operations
 */
@Dao
interface AttendeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendees(attendees: List<AttendeeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendee(attendee: AttendeeEntity)

    @Query("SELECT * FROM attendees WHERE event_id = :eventId ORDER BY name ASC")
    fun getAttendeesByEvent(eventId: String): Flow<List<AttendeeEntity>>

    @Query("SELECT * FROM attendees ORDER BY synced_at DESC")
    fun getAllAttendees(): Flow<List<AttendeeEntity>>

    @Query("SELECT COUNT(*) FROM attendees WHERE event_id = :eventId")
    suspend fun getAttendeeCount(eventId: String): Int

    @Query("DELETE FROM attendees WHERE event_id = :eventId")
    suspend fun deleteAttendeesByEvent(eventId: String)

    @Query("DELETE FROM attendees")
    suspend fun deleteAllAttendees()

    @Query("SELECT * FROM attendees WHERE email = :email LIMIT 1")
    suspend fun findAttendeeByEmail(email: String): AttendeeEntity?
}
