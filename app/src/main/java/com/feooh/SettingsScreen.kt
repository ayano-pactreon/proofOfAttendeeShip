package com.feooh

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feooh.viewmodel.AttendeeViewModel

@Composable
fun SettingsScreen(viewModel: AttendeeViewModel = viewModel()) {
    var apiKey by remember { mutableStateOf(BuildConfig.DEFAULT_LUMA_API_KEY) }
    var eventId by remember { mutableStateOf(BuildConfig.DEFAULT_LUMA_EVENT_ID) }
    var eventName by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    // Reset UI state when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetUiState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Luma Sync Settings",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("Luma API Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            supportingText = { Text("Requires Luma Plus subscription") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = eventId,
            onValueChange = { eventId = it },
            label = { Text("Event ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("Event API ID from Luma") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Verify API Key Button
        Button(
            onClick = { viewModel.verifyApiKey(apiKey) },
            modifier = Modifier.fillMaxWidth(),
            enabled = apiKey.isNotBlank() && uiState !is AttendeeViewModel.UiState.Loading
        ) {
            Text("Verify API Key")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Download Attendees Button
        Button(
            onClick = { viewModel.downloadAttendees(eventId, apiKey, eventName.ifBlank { null }) },
            modifier = Modifier.fillMaxWidth(),
            enabled = apiKey.isNotBlank() && eventId.isNotBlank() && uiState !is AttendeeViewModel.UiState.Loading
        ) {
            Text("Download Attendees")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Display
        when (val state = uiState) {
            is AttendeeViewModel.UiState.Idle -> {
                // No message
            }
            is AttendeeViewModel.UiState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.message, color = MaterialTheme.colorScheme.primary)
            }
            is AttendeeViewModel.UiState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        state.message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            is AttendeeViewModel.UiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        state.message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Instructions:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "1. Get your Luma API key from your Luma dashboard (requires Luma Plus)\n" +
                            "2. Find your Event ID in the Luma event URL or dashboard\n" +
                            "3. Click 'Verify API Key' to test authentication\n" +
                            "4. Click 'Download Attendees' to sync attendee list\n" +
                            "5. View attendees in the 'Users' tab",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
