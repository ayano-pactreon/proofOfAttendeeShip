package com.feooh.data.api

import com.feooh.data.model.EarnActionsResponse
import com.feooh.data.model.EarnRequest
import com.feooh.data.model.EarnResponse
import com.feooh.data.model.MerchandiseListResponse
import com.feooh.data.model.RedeemRequest
import com.feooh.data.model.RedeemResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Merchandise API service interface
 * Base URL: configured in RetrofitClient
 */
interface MerchandiseApiService {

    /**
     * Get list of available merchandise
     * @param authToken Bearer token for authentication
     */
    @GET("api/points/merchandise")
    suspend fun getMerchandise(
        @Header("Authorization") authToken: String
    ): MerchandiseListResponse

    /**
     * Redeem merchandise using wallet address
     * @param authToken Bearer token for authentication
     * @param redeemRequest Contains wallet_address and merch_code
     */
    @POST("api/points/redeem")
    suspend fun redeemMerchandise(
        @Header("Authorization") authToken: String,
        @Body redeemRequest: RedeemRequest
    ): RedeemResponse

    /**
     * Get list of available earn actions
     * @param authToken Bearer token for authentication
     */
    @GET("api/points/actions")
    suspend fun getEarnActions(
        @Header("Authorization") authToken: String
    ): EarnActionsResponse

    /**
     * Earn points by completing an action
     * @param authToken Bearer token for authentication
     * @param earnRequest Contains wallet_address and action_code
     */
    @POST("api/points/earn")
    suspend fun earnPoints(
        @Header("Authorization") authToken: String,
        @Body earnRequest: EarnRequest
    ): EarnResponse
}