package com.feooh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feooh.BuildConfig
import com.feooh.data.api.RetrofitClient
import com.feooh.data.model.EarnAction
import com.feooh.data.model.EarnRequest
import com.feooh.data.model.MerchandiseItem
import com.feooh.data.model.RedeemRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for Merchandise screen
 */
sealed class MerchandiseUiState {
    object Idle : MerchandiseUiState()
    data class Loading(val message: String) : MerchandiseUiState()
    data class Success(val message: String) : MerchandiseUiState()
    data class Error(val message: String) : MerchandiseUiState()
}

/**
 * ViewModel for managing merchandise data and redemption
 */
class MerchandiseViewModel(application: Application) : AndroidViewModel(application) {

    private val merchandiseApiService = RetrofitClient.merchandiseApiService

    // Bearer token from BuildConfig
    private val bearerToken = "Bearer ${BuildConfig.BACKEND_API_TOKEN}"

    // UI State
    private val _uiState = MutableStateFlow<MerchandiseUiState>(MerchandiseUiState.Idle)
    val uiState: StateFlow<MerchandiseUiState> = _uiState.asStateFlow()

    // Merchandise list
    private val _merchandise = MutableStateFlow<List<MerchandiseItem>>(emptyList())
    val merchandise: StateFlow<List<MerchandiseItem>> = _merchandise.asStateFlow()

    // Earn actions list
    private val _earnActions = MutableStateFlow<List<EarnAction>>(emptyList())
    val earnActions: StateFlow<List<EarnAction>> = _earnActions.asStateFlow()

    /**
     * Load merchandise list from API
     */
    fun loadMerchandise() {
        viewModelScope.launch {
            try {
                _uiState.value = MerchandiseUiState.Loading("Loading merchandise...")
                val response = merchandiseApiService.getMerchandise(bearerToken)
                if (response.success) {
                    _merchandise.value = response.data
                    _uiState.value = MerchandiseUiState.Idle
                } else {
                    _uiState.value = MerchandiseUiState.Error("Failed to load merchandise")
                }
            } catch (e: Exception) {
                _uiState.value = MerchandiseUiState.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Load earn actions list from API
     */
    fun loadEarnActions() {
        viewModelScope.launch {
            try {
                _uiState.value = MerchandiseUiState.Loading("Loading earn actions...")
                val response = merchandiseApiService.getEarnActions(bearerToken)
                if (response.success) {
                    _earnActions.value = response.data
                    _uiState.value = MerchandiseUiState.Idle
                } else {
                    _uiState.value = MerchandiseUiState.Error("Failed to load earn actions")
                }
            } catch (e: Exception) {
                _uiState.value = MerchandiseUiState.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Redeem merchandise using wallet address
     */
    fun redeemMerchandise(walletAddress: String, merchCode: String) {
        viewModelScope.launch {
            try {
                _uiState.value = MerchandiseUiState.Loading("Redeeming...")
                val request = RedeemRequest(walletAddress, merchCode)
                val response = merchandiseApiService.redeemMerchandise(bearerToken, request)

                if (response.success) {
                    val newBalance = response.data?.newBalance ?: 0
                    _uiState.value = MerchandiseUiState.Success(
                        "${response.message}\nNew balance: $newBalance points"
                    )
                } else {
                    val balance = response.data?.balance ?: 0
                    val cost = response.data?.cost ?: 0
                    _uiState.value = MerchandiseUiState.Error(
                        "${response.message}\nBalance: $balance, Cost: $cost"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MerchandiseUiState.Error("Redemption failed: ${e.message}")
            }
        }
    }

    /**
     * Earn points by completing an action
     */
    fun earnPoints(walletAddress: String, actionCode: String) {
        viewModelScope.launch {
            try {
                _uiState.value = MerchandiseUiState.Loading("Earning points...")
                val request = EarnRequest(walletAddress, actionCode)
                val response = merchandiseApiService.earnPoints(bearerToken, request)

                if (response.success) {
                    val newBalance = response.data?.newBalance ?: 0
                    _uiState.value = MerchandiseUiState.Success(
                        "${response.message}\nNew balance: $newBalance points"
                    )
                } else {
                    _uiState.value = MerchandiseUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = MerchandiseUiState.Error("Earn failed: ${e.message}")
            }
        }
    }

    /**
     * Reset UI state to idle
     */
    fun resetUiState() {
        _uiState.value = MerchandiseUiState.Idle
    }
}
