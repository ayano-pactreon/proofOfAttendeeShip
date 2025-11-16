package com.feooh.ui

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.feooh.nfc.NfcHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NfcClearDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val nfcHandler = remember(context) {
        if (context is Activity) NfcHandler(context) else null
    }

    var isSuccess by remember { mutableStateOf<Boolean?>(null) }
    var statusText by remember { mutableStateOf("Tap wristband to clear...") }

    val scope = rememberCoroutineScope()

    // Start NFC clearing immediately
    LaunchedEffect(Unit) {
        nfcHandler?.enableClearing { success, result ->
            nfcHandler.disable()

            scope.launch {
                if (success) {
                    isSuccess = true
                    statusText = "Wristband cleared successfully!"
                    delay(1000)
                    onSuccess()
                } else {
                    isSuccess = false
                    statusText = result ?: "Failed to clear wristband"
                    delay(1200)
                    onError(statusText)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            nfcHandler?.disable()
            onDismiss()
        },
        confirmButton = {},
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Animated icon transitions
                Crossfade(targetState = isSuccess, label = "icon-state") { state ->
                    when (state) {
                        null -> Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "Scanning NFC",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                        true -> Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                        false -> Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status text
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (isSuccess) {
                        true -> MaterialTheme.colorScheme.primary
                        false -> MaterialTheme.colorScheme.error
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Progress bar during clearing
                if (isSuccess == null) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel button
                if (isSuccess == null) {
                    TextButton(onClick = {
                        nfcHandler?.disable()
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Cancel",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    )
}
