package com.feooh.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.feooh.utils.DemoModeManager

@Composable
fun EasterEggDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var demoModeEnabled by remember { mutableStateOf(DemoModeManager.isDemoModeEnabled(context)) }
    var demoUsername by remember { mutableStateOf(DemoModeManager.getDemoUsername(context)) }
    var demoEmail by remember { mutableStateOf(DemoModeManager.getDemoEmail(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = "Developer Options",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Developer Options",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "You've discovered the easter egg! 🎉",
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider()

                // Demo Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Demo Mode",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hide sensitive user information",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = demoModeEnabled,
                        onCheckedChange = { enabled ->
                            demoModeEnabled = enabled
                            DemoModeManager.setDemoMode(context, enabled)
                        }
                    )
                }

                if (demoModeEnabled) {
                    HorizontalDivider()

                    // Demo Username
                    OutlinedTextField(
                        value = demoUsername,
                        onValueChange = {
                            demoUsername = it
                            DemoModeManager.setDemoCredentials(context, it, demoEmail)
                        },
                        label = { Text("Demo Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Demo Email
                    OutlinedTextField(
                        value = demoEmail,
                        onValueChange = {
                            demoEmail = it
                            DemoModeManager.setDemoCredentials(context, demoUsername, it)
                        },
                        label = { Text("Demo Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Demo mode is active. User names and emails will be replaced with the values above.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
