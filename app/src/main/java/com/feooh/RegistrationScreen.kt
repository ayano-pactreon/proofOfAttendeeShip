// In RegistrationScreen.kt
package com.feooh

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.feooh.data.api.BackendRetrofitClient
import com.feooh.data.model.GuestData
import com.feooh.data.model.RegisterGuestRequest
import com.feooh.data.model.RegisterGuestResponse
import com.feooh.ui.EasterEggScreen
import com.feooh.ui.PoweredByFooter
import com.feooh.ui.SubZeroHeader
import com.feooh.ui.UserDetailsScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RegistrationScreen() {
    var guestData by remember { mutableStateOf<GuestData?>(null) }
    var showEasterEgg by remember { mutableStateOf(false) }

    when {
        showEasterEgg -> {
            // Show the easter egg developer options screen
            EasterEggScreen(
                onBack = { showEasterEgg = false }
            )
        }
        guestData != null -> {
            // Show the user details screen after registration
            UserDetailsScreen(
                guestData = guestData!!,
                onBackToRegistration = { guestData = null }
            )
        }
        else -> {
            // Show the registration scanner screen
            RegistrationScannerScreen(
                onGuestRegistered = { data -> guestData = data },
                onEasterEggTriggered = { showEasterEgg = true }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RegistrationScannerScreen(
    onGuestRegistered: (GuestData) -> Unit,
    onEasterEggTriggered: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showScanner by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (showScanner && cameraPermissionState.status.isGranted) {
            // Show QR Code Scanner
            QrCodeScanner { qrCodeUrl ->
                showScanner = false
                isLoading = true

                // Send to backend API
                scope.launch {
                    try {
                        val backendApi = BackendRetrofitClient.getBackendApiService()
                        val request = RegisterGuestRequest(
                            eventId = BuildConfig.DEFAULT_LUMA_EVENT_ID,
                            qrCodeUrl = qrCodeUrl
                        )

                        val response = backendApi.registerGuest(request)

                        isLoading = false

                        if (response.success && response.data != null) {
                            // Success - navigate to user details screen
                            onGuestRegistered(response.data)
                        } else {
                            // Failed - show error message from API
                            isError = true
                            resultMessage = response.message ?: "Registration failed"
                        }
                    } catch (e: HttpException) {
                        isLoading = false
                        isError = true

                        // Handle HTTP errors
                        resultMessage = if (e.code() == 500) {
                            "Server error. Please try again later."
                        } else {
                            // Try to parse message from error response body
                            try {
                                val errorBody = e.response()?.errorBody()?.string()
                                if (errorBody != null) {
                                    val json = Json { ignoreUnknownKeys = true }
                                    val errorResponse = json.decodeFromString<RegisterGuestResponse>(errorBody)
                                    errorResponse.message ?: "Registration failed"
                                } else {
                                    "Registration failed (${e.code()})"
                                }
                            } catch (ex: Exception) {
                                "Registration failed (${e.code()})"
                            }
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        isError = true
                        resultMessage = "Connection error: ${e.message}"
                    }
                }
            }
        } else {
            // Show main UI with Scan button
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with logos at top
                SubZeroHeader(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp),
                    onEasterEggTriggered = onEasterEggTriggered
                )

                // Main content centered
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "Guest Registration",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan Luma QR Code to register",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Scan Button
                    Button(
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            resultMessage = null
                            showScanner = true
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Luma QR Code")
                    }
                    }

                    // Result Message (only for errors)
                    if (resultMessage != null && isError) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = resultMessage!!,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Event Info
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Event: ${BuildConfig.DEFAULT_LUMA_EVENT_ID}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Powered by Footer at bottom
                PoweredByFooter(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}
