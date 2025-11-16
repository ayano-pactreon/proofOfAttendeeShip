package com.feooh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feooh.data.api.RetrofitClient
import com.feooh.data.database.AppDatabase
import com.feooh.data.database.AttendeeEntity
import com.feooh.data.database.EventEntity
import com.feooh.data.repository.AttendeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing attendee data and sync operations
 */
class AttendeeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AttendeeRepository(
        lumaApiService = RetrofitClient.lumaApiService,
        attendeeDao = database.attendeeDao(),
        eventDao = database.eventDao()
    )

    // UI State
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Attendees list
    private val _attendees = MutableStateFlow<List<AttendeeEntity>>(emptyList())
    val attendees: StateFlow<List<AttendeeEntity>> = _attendees.asStateFlow()

    // Events list
    private val _events = MutableStateFlow<List<EventEntity>>(emptyList())
    val events: StateFlow<List<EventEntity>> = _events.asStateFlow()

    init {
        // Load all attendees from database
        viewModelScope.launch {
            repository.getAllAttendees().collect { list ->
                _attendees.value = list
            }
        }

        // Load all events from database
        viewModelScope.launch {
            repository.getAllEvents().collect { list ->
                _events.value = list
            }
        }
    }

    /**
     * Verify Luma API key
     */
    fun verifyApiKey(apiKey: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading("Verifying API key...")
            val result = repository.verifySelf(apiKey)
            _uiState.value = if (result.isSuccess) {
                UiState.Success("API key verified: ${result.getOrNull()}")
            } else {
                UiState.Error("API verification failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Download attendees from Luma API
     */
    fun downloadAttendees(eventId: String, apiKey: String, eventName: String? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading("Downloading attendees...")
            val result = repository.downloadAndSaveAttendees(eventId, apiKey, eventName)
            _uiState.value = if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                UiState.Success("Successfully downloaded $count attendees")
            } else {
                UiState.Error("Download failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Load attendees for a specific event
     */
    fun loadAttendeesByEvent(eventId: String) {
        viewModelScope.launch {
            repository.getAttendeesByEvent(eventId).collect { list ->
                _attendees.value = list
            }
        }
    }

    /**
     * Reset UI state to idle
     */
    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    /**
     * UI State sealed class
     */
    sealed class UiState {
        data object Idle : UiState()
        data class Loading(val message: String) : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}
