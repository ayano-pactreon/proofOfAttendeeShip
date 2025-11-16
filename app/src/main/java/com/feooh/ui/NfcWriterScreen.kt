package com.feooh.ui

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.feooh.nfc.NfcHandler

@Composable
fun NfcWriterDialog(
    walletAddress: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val nfcHandler = remember(context) {
        if (context is Activity) NfcHandler(context) else null
    }
    var nfcStatus by remember { mutableStateOf("Ready to write") }
    var isWriting by remember { mutableStateOf(false) }

    // Enable NFC writing mode
    DisposableEffect(Unit) {
        if (nfcHandler != null && nfcHandler.isNfcAvailable()) {
            isWriting = true
            nfcStatus = "Waiting for tag..."

            nfcHandler.enableWriting(walletAddress) { success, message ->
                isWriting = false
                if (success) {
                    nfcStatus = message ?: "Success!"
                    onSuccess()
                } else {
                    nfcStatus = message ?: "Write failed"
                    onError(message ?: "Write failed")
                }
            }
        }

        onDispose {
            nfcHandler?.disable()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Animated NFC icon
                NfcAnimatedIcon(isWriting = isWriting)

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = if (nfcHandler == null || !nfcHandler.isNfcAvailable()) {
                        "NFC Not Available"
                    } else {
                        "Ready to Write"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (nfcHandler == null || !nfcHandler.isNfcAvailable()) {
                        "NFC is not enabled. Please enable it in your device settings."
                    } else {
                        "Hold your wristband near the back of your phone"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status text
                Text(
                    text = nfcStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isWriting) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center
                )

                if (isWriting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun NfcAnimatedIcon(isWriting: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Animated circles
        if (!isWriting) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size.minDimension
                drawCircle(
                    color = Color(0xFF6200EE).copy(alpha = alpha),
                    radius = canvasSize / 2 * scale,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // NFC Icon
        Icon(
            imageVector = Icons.Default.Nfc,
            contentDescription = "NFC",
            modifier = Modifier.size(64.dp),
            tint = if (isWriting) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            }
        )
    }
}