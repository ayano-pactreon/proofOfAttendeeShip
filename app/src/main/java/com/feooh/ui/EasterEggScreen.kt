package com.feooh.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feooh.utils.DemoModeManager

/**
 * Easter Egg Screen - Developer Options
 * Full screen version for configuring demo mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasterEggScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var demoModeEnabled by remember { mutableStateOf(DemoModeManager.isDemoModeEnabled(context)) }
    var demoUsername by remember { mutableStateOf(DemoModeManager.getDemoUsername(context)) }
    var demoEmail by remember { mutableStateOf(DemoModeManager.getDemoEmail(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Options") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Easter Egg Icon and Title
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = "Developer Options",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "You've discovered the easter egg! 🎉",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()

            // Demo Mode Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Demo Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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
                                text = "ℹ️ Demo mode is active. User names and emails will be replaced with the values above when displaying user details.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back Button
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}
