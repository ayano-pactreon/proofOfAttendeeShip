package com.feooh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for backend API guest registration
 */
@Serializable
data class RegisterGuestRequest(
    @SerialName("event_id") val eventId: String,
    @SerialName("qr_code_url") val qrCodeUrl: String
)

/**
 * Guest data returned from backend API
 */
@Serializable
data class GuestData(
    @SerialName("luma_guest_id") val lumaGuestId: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("user_type") val userType: String,
    @SerialName("user_type_label") val userTypeLabel: String
)

/**
 * Response from backend API for guest registration
 */
@Serializable
data class RegisterGuestResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: GuestData? = null
)

/**
 * Request body for connecting wallet to guest
 */
@Serializable
data class ConnectWalletRequest(
    @SerialName("luma_guest_id") val lumaGuestId: Int,
    @SerialName("wallet_address") val walletAddress: String,
    @SerialName("wallet_type") val walletType: String = "polkadot"
)

/**
 * Response from backend API for wallet connection
 */
@Serializable
data class ConnectWalletResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null
)

/**
 * Redeemed item details
 */
@Serializable
data class RedeemedItem(
    @SerialName("transaction_id") val transactionId: Int,
    @SerialName("merchandise_id") val merchandiseId: Int,
    @SerialName("merchandise_code") val merchandiseCode: String,
    @SerialName("merchandise_name") val merchandiseName: String,
    @SerialName("points_spent") val pointsSpent: Int,
    @SerialName("redeemed_at") val redeemedAt: String
)

/**
 * Earned action details
 */
@Serializable
data class EarnedAction(
    @SerialName("transaction_id") val transactionId: Int,
    @SerialName("action_id") val actionId: Int,
    @SerialName("action_code") val actionCode: String,
    @SerialName("action_name") val actionName: String,
    @SerialName("points_earned") val pointsEarned: Int,
    @SerialName("earned_at") val earnedAt: String
)

/**
 * Available action details
 */
@Serializable
data class AvailableAction(
    @SerialName("action_id") val actionId: Int,
    @SerialName("action_code") val actionCode: String,
    @SerialName("action_name") val actionName: String,
    @SerialName("points") val points: Int
)

/**
 * Guest data returned from wallet lookup
 */
@Serializable
data class WalletLookupGuestData(
    @SerialName("user_id") val userId: Int,
    @SerialName("name") val name: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("email") val email: String,
    @SerialName("user_type") val userType: String,
    @SerialName("luma_guest_id") val lumaGuestId: Int,
    @SerialName("wallet_address") val walletAddress: String,
    @SerialName("wallet_type") val walletType: String,
    @SerialName("balance") val balance: Int? = null,
    @SerialName("redeemed_items") val redeemedItems: List<RedeemedItem>? = null,
    @SerialName("earned_actions") val earnedActions: List<EarnedAction>? = null,
    @SerialName("available_actions") val availableActions: List<AvailableAction>? = null
)

/**
 * Response from backend API for guest lookup by wallet
 */
@Serializable
data class WalletLookupResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: WalletLookupGuestData? = null
)


/**
 * Request body for updating a Luma guest's status.
 */
@Serializable
data class UpdateGuestStatusRequest (
    @SerialName("status") val status: String,
    @SerialName("notes") val notes: String? = null
)

/**
 * Response returned after updating a guest's status.
 */
@Serializable
data class UpdateGuestStatusResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null
)