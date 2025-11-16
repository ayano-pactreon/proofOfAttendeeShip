package com.feooh.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing event attendees
 */
@Entity(tableName = "attendees")
data class AttendeeEntity(
    @PrimaryKey
    @ColumnInfo(name = "guest_id")
    val guestId: String,

    @ColumnInfo(name = "event_id")
    val eventId: String,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "email")
    val email: String?,

    @ColumnInfo(name = "approval_status")
    val approvalStatus: String?,

    @ColumnInfo(name = "registration_status")
    val registrationStatus: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: String?,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String?,

    @ColumnInfo(name = "synced_at")
    val syncedAt: Long = System.currentTimeMillis()
)
