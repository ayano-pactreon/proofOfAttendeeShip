package com.feooh.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response model for merchandise list API
 */
@Serializable
data class MerchandiseListResponse(
    val success: Boolean,
    val data: List<MerchandiseItem>
)

/**
 * Individual merchandise item
 */
@Serializable
data class MerchandiseItem(
    val id: Int,
    val code: String,
    val name: String,
    @SerialName("points_cost")
    val pointsCost: Int,
    val stock: Int? = null
)

/**
 * Request model for redeeming merchandise
 */
@Serializable
data class RedeemRequest(
    @SerialName("wallet_address")
    val walletAddress: String,
    @SerialName("merch_code")
    val merchCode: String
)

/**
 * Response model for successful redemption
 */
@Serializable
data class RedeemResponse(
    val success: Boolean,
    val message: String,
    val data: RedeemData? = null
)

/**
 * Redemption data (success case)
 */
@Serializable
data class RedeemData(
    @SerialName("transaction_id")
    val transactionId: Int? = null,
    @SerialName("new_balance")
    val newBalance: Int? = null,
    val balance: Int? = null,
    val cost: Int? = null
)

/**
 * Response model for earn actions list API
 */
@Serializable
data class EarnActionsResponse(
    val success: Boolean,
    val data: List<EarnAction>
)

/**
 * Individual earn action item
 */
@Serializable
data class EarnAction(
    val id: Int,
    val code: String,
    val name: String,
    val points: Int
)

/**
 * Request model for earning points
 */
@Serializable
data class EarnRequest(
    @SerialName("wallet_address")
    val walletAddress: String,
    @SerialName("action_code")
    val actionCode: String
)

/**
 * Response model for earn points API
 */
@Serializable
data class EarnResponse(
    val success: Boolean,
    val message: String,
    val data: EarnData? = null
)

/**
 * Earn response data (success case)
 */
@Serializable
data class EarnData(
    @SerialName("transaction_id")
    val transactionId: Int? = null,
    @SerialName("new_balance")
    val newBalance: Int? = null
)