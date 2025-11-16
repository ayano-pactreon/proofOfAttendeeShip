package com.feooh.data.api

import com.feooh.data.model.LumaGuestsResponse
import com.feooh.data.model.LumaUserResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Luma API service interface
 * Base URL: https://public-api.luma.com
 */
interface LumaApiService {

    /**
     * Get current user information to verify API key
     * Equivalent to: curl -H "x-luma-api-key: $LUMA_API_KEY" https://public-api.luma.com/v1/user/get-self
     */
    @GET("v1/user/get-self")
    suspend fun getSelf(
        @Header("x-luma-api-key") apiKey: String
    ): LumaUserResponse

    /**
     * Get list of guests for a specific event
     * @param eventId The event API ID
     * @param apiKey The Luma API key
     * @param cursor Pagination cursor for next page
     */
    @GET("v1/event/get-guests")
    suspend fun getEventGuests(
        @Query("event_id") eventId: String,
        @Header("x-luma-api-key") apiKey: String,
        @Query("cursor") cursor: String? = null,
        @Header("accept") accept: String = "application/json"
    ): LumaGuestsResponse
}
