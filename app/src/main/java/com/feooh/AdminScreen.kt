package com.feooh

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.feooh.nfc.NfcHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import com.feooh.ui.NfcClearDialog
import com.feooh.ui.PoweredByFooter
import com.feooh.ui.SubZeroHeader

@Composable
fun AdminScreen() {
    // Admin authentication state
    var isAuthenticated by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    // Show password prompt if not authenticated
    if (!isAuthenticated) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with logos at top
            SubZeroHeader(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            )

            // Password prompt centered
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Admin Access",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter admin password to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (password == BuildConfig.ADMIN_PASSWORD) {
                                isAuthenticated = true
                            } else {
                                showError = true
                            }
                        }
                    ),
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Incorrect password", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (password == BuildConfig.ADMIN_PASSWORD) {
                            isAuthenticated = true
                        } else {
                            showError = true
                        }
                    },
                    enabled = password.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Enter")
                }
            }

            // Powered by Footer at bottom
            PoweredByFooter(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
        return
    }

    // ✅ Authenticated - show admin content
    val context = LocalContext.current
    val nfcHandler = remember(context) {
        if (context is Activity) NfcHandler(context) else null
    }

    // ✅ Dialog state
    var showNfcClearDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with logos at top
        SubZeroHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        // Main content centered
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = "NFC",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Clear Wristband",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap the wristband to erase existing data",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ✅ Instead of calling enableClearing() directly,
            // show the NfcClearDialog
            Button(
                onClick = {
                    if (nfcHandler != null && nfcHandler.isNfcAvailable()) {
                        showNfcClearDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = nfcHandler != null && nfcHandler.isNfcAvailable()
            ) {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = "Clear",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear / Format Wristband")
            }

            if (nfcHandler == null || !nfcHandler.isNfcAvailable()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "NFC is not available. Please enable it in settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Powered by Footer at bottom
        PoweredByFooter(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // ✅ Show the NFC clearing dialog
    if (showNfcClearDialog) {
        NfcClearDialog(
            onDismiss = { showNfcClearDialog = false },
            onSuccess = {
                showNfcClearDialog = false
                println("✅ Tag cleared successfully")
            },
            onError = { error ->
                showNfcClearDialog = false
                println("❌ Clear error: $error")
            }
        )
    }
}
