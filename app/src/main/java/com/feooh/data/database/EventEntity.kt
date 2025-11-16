package com.feooh.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing event information
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    val eventId: String,

    @ColumnInfo(name = "event_name")
    val eventName: String?,

    @ColumnInfo(name = "last_synced")
    val lastSynced: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "total_attendees")
    val totalAttendees: Int = 0
)
