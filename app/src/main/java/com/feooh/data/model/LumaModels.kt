package com.feooh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from Luma API for user self information
 */
@Serializable
data class LumaUserResponse(
    @SerialName("api_id") val apiId: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("email") val email: String? = null
)

/**
 * Response from Luma API for event guests
 */
@Serializable
data class LumaGuestsResponse(
    @SerialName("entries") val entries: List<LumaGuestEntry> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_cursor") val nextCursor: String? = null
)

/**
 * Wrapper for each entry in the guests list
 */
@Serializable
data class LumaGuestEntry(
    @SerialName("api_id") val apiId: String,
    @SerialName("guest") val guest: LumaGuest
)

/**
 * Individual guest details from Luma API
 */
@Serializable
data class LumaGuest(
    @SerialName("api_id") val guestId: String,
    @SerialName("user_name") val name: String? = null,
    @SerialName("user_email") val email: String? = null,
    @SerialName("approval_status") val approvalStatus: String? = null,
    @SerialName("registered_at") val registeredAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("checked_in_at") val checkedInAt: String? = null
)
