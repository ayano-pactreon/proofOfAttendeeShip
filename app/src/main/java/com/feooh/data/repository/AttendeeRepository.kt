package com.feooh.data.repository

import com.feooh.data.api.LumaApiService
import com.feooh.data.database.AttendeeDao
import com.feooh.data.database.AttendeeEntity
import com.feooh.data.database.EventDao
import com.feooh.data.database.EventEntity
import com.feooh.data.model.LumaGuest
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing attendee data from Luma API and local database
 */
class AttendeeRepository(
    private val lumaApiService: LumaApiService,
    private val attendeeDao: AttendeeDao,
    private val eventDao: EventDao
) {

    /**
     * Verify API key by calling get-self endpoint
     */
    suspend fun verifySelf(apiKey: String): Result<String> {
        return try {
            val response = lumaApiService.getSelf(apiKey)
            Result.success(response.name ?: response.email ?: "User verified")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Download all attendees for an event and save to database
     * Handles pagination automatically
     */
    suspend fun downloadAndSaveAttendees(
        eventId: String,
        apiKey: String,
        eventName: String? = null
    ): Result<Int> {
        return try {
            var totalDownloaded = 0
            var cursor: String? = null
            var hasMore = true

            // Clear existing attendees for this event
            attendeeDao.deleteAttendeesByEvent(eventId)

            // Fetch all pages
            while (hasMore) {
                val response = lumaApiService.getEventGuests(eventId, apiKey, cursor)

                // Convert API models to database entities
                val entities = response.entries.map { it.guest.toEntity(eventId) }
                attendeeDao.insertAttendees(entities)

                totalDownloaded += entities.size
                cursor = response.nextCursor
                hasMore = response.hasMore && cursor != null
            }

            // Update event info
            val event = EventEntity(
                eventId = eventId,
                eventName = eventName,
                lastSynced = System.currentTimeMillis(),
                totalAttendees = totalDownloaded
            )
            eventDao.insertEvent(event)

            Result.success(totalDownloaded)
        } catch (e: Exception) {
            android.util.Log.e("AttendeeRepository", "Download failed for event $eventId", e)
            Result.failure(Exception("${e.javaClass.simpleName}: ${e.message}"))
        }
    }

    /**
     * Get attendees for a specific event from local database
     */
    fun getAttendeesByEvent(eventId: String): Flow<List<AttendeeEntity>> {
        return attendeeDao.getAttendeesByEvent(eventId)
    }

    /**
     * Get all attendees from local database
     */
    fun getAllAttendees(): Flow<List<AttendeeEntity>> {
        return attendeeDao.getAllAttendees()
    }

    /**
     * Get all events from local database
     */
    fun getAllEvents(): Flow<List<EventEntity>> {
        return eventDao.getAllEvents()
    }

    /**
     * Find attendee by email
     */
    suspend fun findAttendeeByEmail(email: String): AttendeeEntity? {
        return attendeeDao.findAttendeeByEmail(email)
    }

    /**
     * Add a manually registered attendee
     */
    suspend fun addManualAttendee(
        eventId: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        val attendee = AttendeeEntity(
            guestId = "manual_${System.currentTimeMillis()}",
            eventId = eventId,
            name = "$firstName $lastName",
            email = email,
            approvalStatus = "approved",
            registrationStatus = "registered",
            createdAt = null,
            updatedAt = null,
            syncedAt = System.currentTimeMillis()
        )
        attendeeDao.insertAttendee(attendee)
    }

    /**
     * Convert Luma API guest to database entity
     */
    private fun LumaGuest.toEntity(eventId: String): AttendeeEntity {
        return AttendeeEntity(
            guestId = this.guestId,
            eventId = eventId,
            name = this.name,
            email = this.email,
            approvalStatus = this.approvalStatus,
            registrationStatus = if (this.registeredAt != null) "registered" else "pending",
            createdAt = this.createdAt,
            updatedAt = this.checkedInAt
        )
    }
}
