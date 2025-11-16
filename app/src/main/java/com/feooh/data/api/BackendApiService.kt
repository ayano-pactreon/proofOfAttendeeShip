package com.feooh.data.api

import com.feooh.data.model.ConnectWalletRequest
import com.feooh.data.model.ConnectWalletResponse
import com.feooh.data.model.RegisterGuestRequest
import com.feooh.data.model.RegisterGuestResponse
import com.feooh.data.model.UpdateGuestStatusRequest
import com.feooh.data.model.UpdateGuestStatusResponse
import com.feooh.data.model.WalletLookupResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Backend API service interface
 * Base URL configured from BuildConfig.BACKEND_API_URL
 */
interface BackendApiService {

    /**
     * Register a guest by scanning QR code
     * @param request The registration request containing event_id and qr_code_url
     */
    @POST("api/luma/guests/register")
    suspend fun registerGuest(
        @Body request: RegisterGuestRequest
    ): RegisterGuestResponse

    /**
     * Connect a wallet address to a guest
     * @param request The wallet connection request containing luma_guest_id, wallet_address, and wallet_type
     */
    @POST("api/luma/guests/connect-wallet")
    suspend fun connectWallet(
        @Body request: ConnectWalletRequest
    ): ConnectWalletResponse

    /**
     * Look up a guest by wallet address
     * @param walletAddress The wallet address to look up
     * @param walletType The wallet type (defaults to polkadot)
     */
    @GET("api/luma/guests/by-wallet")
    suspend fun getGuestByWallet(
        @Query("wallet_address") walletAddress: String,
        @Query("wallet_type") walletType: String = "polkadot"
    ): WalletLookupResponse

    /**
     * Update a guest's registration status
     * @param guestId The Luma guest ID
     * @param request The status update payload containing status and optional notes
     */
    @PUT("api/luma/guests/{guest_id}/status")
    suspend fun updateGuestStatus(
        @Path("guest_id") guestId: Int,
        @Body request: UpdateGuestStatusRequest
    ): UpdateGuestStatusResponse

}