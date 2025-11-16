package com.feooh.ui

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feooh.QrCodeScanner
import com.feooh.data.api.BackendRetrofitClient
import com.feooh.data.enums.LumaGuestStatus
import com.feooh.data.model.ConnectWalletRequest
import com.feooh.data.model.ConnectWalletResponse
import com.feooh.data.model.GuestData
import com.feooh.data.model.UpdateGuestStatusRequest
import com.feooh.utils.DemoModeManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UserDetailsScreen(
    guestData: GuestData,
    onBackToRegistration: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showWalletScanner by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var walletConnected by remember { mutableStateOf(false) }
    var walletAddress by remember { mutableStateOf<String?>(null) }
    var showNfcWriter by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var demoMode by remember { mutableStateOf(DemoModeManager.isDemoModeEnabled(context)) }
    var demoUsername by remember { mutableStateOf(DemoModeManager.getDemoUsername(context)) }
    var demoEmail by remember { mutableStateOf(DemoModeManager.getDemoEmail(context)) }

    // Observe demo mode changes
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            demoMode = DemoModeManager.isDemoModeEnabled(context)
            demoUsername = DemoModeManager.getDemoUsername(context)
            demoEmail = DemoModeManager.getDemoEmail(context)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (showWalletScanner && cameraPermissionState.status.isGranted) {
            // Show Wallet QR Scanner
            QrCodeScanner { scannedAddress ->
                showWalletScanner = false
                isLoading = true

                // Send wallet address to backend
                scope.launch {
                    try {
                        val backendApi = BackendRetrofitClient.getBackendApiService()
                        val request = ConnectWalletRequest(
                            lumaGuestId = guestData.lumaGuestId,
                            walletAddress = scannedAddress,
                            walletType = "polkadot"
                        )

                        val response = backendApi.connectWallet(request)

                        isLoading = false

                        if (response.success) {
                            walletConnected = true
                            walletAddress = scannedAddress
                            isError = false
                            resultMessage = response.message ?: "Wallet connected successfully!"
                        } else {
                            isError = true
                            resultMessage = response.message ?: "Failed to connect wallet"
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
                                    val errorResponse = json.decodeFromString<ConnectWalletResponse>(errorBody)
                                    errorResponse.message ?: "Failed to connect wallet"
                                } else {
                                    "Failed to connect wallet (${e.code()})"
                                }
                            } catch (ex: Exception) {
                                "Failed to connect wallet (${e.code()})"
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
            // Show User Details UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Success Icon
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Registration Successful!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Guest Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Name",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (demoMode) demoUsername else guestData.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        HorizontalDivider()

                        // Email
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Email",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (demoMode) demoEmail else guestData.email,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        HorizontalDivider()

                        // User Type
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Type",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Type",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = guestData.userTypeLabel,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        // Wallet Address (only show if connected)
                        if (walletConnected && walletAddress != null) {
                            HorizontalDivider()

                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Wallet Address",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = walletAddress!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 3
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Connect Wallet Button
                if (!walletConnected) {
                    Button(
                        onClick = {
                            if (cameraPermissionState.status.isGranted) {
                                resultMessage = null
                                showWalletScanner = true
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
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Scan Wallet",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Wallet QR Code")
                        }
                    }
                }

                // Result Message
                if (resultMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isError) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                    ) {
                        Text(
                            text = resultMessage!!,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }

                // Setup Wristband Button (only show if wallet connected)
                if (walletConnected && walletAddress != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showNfcWriter = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "Setup Wristband",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Setup Wristband")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Back Button
                TextButton(
                    onClick = onBackToRegistration,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register Another Guest")
                }
            }
        }

        // NFC Writer Dialog
        if (showNfcWriter && walletAddress != null) {
            NfcWriterDialog(
                walletAddress = walletAddress!!,
                onDismiss = { showNfcWriter = false },
                onSuccess = {
                    showNfcWriter = false
                    isError = false
                    resultMessage = "Wristband setup successful!"

                    scope.launch {
                        try {
                            val backendApi = BackendRetrofitClient.getBackendApiService()
                            val updateStatusRequest = UpdateGuestStatusRequest(
                                status = LumaGuestStatus.NFC_INITIALIZED.value,
                                notes = "NFC tag written successfully"
                            )

                            val response = backendApi.updateGuestStatus(
                                guestId = guestData.lumaGuestId,
                                request = updateStatusRequest
                            )

                            if (response.success) {
                                resultMessage = "Wrist band initialized successfully!"
                                isError = false
                            } else {
                                resultMessage = "Wristband setup successful!"
                                isError = true
                            }
                        } catch (e: Exception) {
                            resultMessage = "Failed to update status: ${e.message}"
                            isError = true
                        }
                    }
                },
                onError = { error ->
                    showNfcWriter = false
                    isError = true
                    resultMessage = error
                }
            )
        }
    }
}