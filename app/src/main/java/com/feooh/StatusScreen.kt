package com.feooh

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feooh.data.api.BackendRetrofitClient
import com.feooh.data.model.AvailableAction
import com.feooh.data.model.EarnedAction
import com.feooh.data.model.RedeemedItem
import com.feooh.data.model.WalletLookupGuestData
import com.feooh.data.model.WalletLookupResponse
import com.feooh.nfc.NfcHandler
import com.feooh.ui.NfcAnimatedIcon
import com.feooh.ui.PoweredByFooter
import com.feooh.ui.SubZeroHeader
import com.feooh.utils.DemoModeManager
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StatusScreen() {
    val context = LocalContext.current
    val nfcHandler = remember(context) {
        if (context is Activity) NfcHandler(context) else null
    }
    var isScanning by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var guestData by remember { mutableStateOf<WalletLookupGuestData?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var nfcDisabledTemporarily by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // NFC scanning logic
    DisposableEffect(isScanning, nfcDisabledTemporarily) {
        if (isScanning && !nfcDisabledTemporarily && nfcHandler != null && nfcHandler.isNfcAvailable()) {
            nfcHandler.enableReading(
                onTagRead = { walletAddress ->
                    // Immediately disable to prevent Android's default handler
                    nfcHandler.disable()
                    nfcDisabledTemporarily = true
                    isScanning = false
                    isLoading = true

                    // Re-enable after 2 seconds
                    scope.launch {
                        kotlinx.coroutines.delay(2000)
                        nfcDisabledTemporarily = false
                    }

                    // Call backend API
                    scope.launch {
                        try {
                            val backendApi = BackendRetrofitClient.getBackendApiService()
                            val response = backendApi.getGuestByWallet(
                                walletAddress = walletAddress,
                                walletType = "polkadot"
                            )

                            isLoading = false

                            if (response.success && response.data != null) {
                                guestData = response.data
                                errorMessage = null
                            } else {
                                errorMessage = response.message ?: "User not found"
                                guestData = null
                            }
                        } catch (e: HttpException) {
                            isLoading = false
                            errorMessage = if (e.code() == 500) {
                                "Server error. Please try again later."
                            } else {
                                try {
                                    val errorBody = e.response()?.errorBody()?.string()
                                    if (errorBody != null) {
                                        val json = Json { ignoreUnknownKeys = true }
                                        val errorResponse = json.decodeFromString<WalletLookupResponse>(errorBody)
                                        errorResponse.message ?: "Failed to look up user"
                                    } else {
                                        "Failed to look up user (${e.code()})"
                                    }
                                } catch (ex: Exception) {
                                    "Failed to look up user (${e.code()})"
                                }
                            }
                            guestData = null
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Connection error: ${e.message}"
                            guestData = null
                        }
                    }
                },
                onError = { error ->
                    isScanning = false
                    errorMessage = error
                    guestData = null
                }
            )
        }

        onDispose {
            nfcHandler?.disable()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isScanning) {
            // Show scanning UI
            ScanningUI(
                onCancel = { isScanning = false }
            )
        } else if (isLoading) {
            // Show loading
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Looking up user...")
            }
        } else if (guestData != null) {
            // Show guest data
            GuestDataDisplay(
                guestData = guestData!!,
                onScanAnother = {
                    guestData = null
                    errorMessage = null
                    isScanning = true
                }
            )
        } else {
            // Show initial state
            InitialScanUI(
                nfcHandler = nfcHandler,
                errorMessage = errorMessage,
                onStartScan = { isScanning = true }
            )
        }
    }
}

@Composable
fun InitialScanUI(
    nfcHandler: NfcHandler?,
    errorMessage: String?,
    onStartScan: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        SubZeroHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = "NFC",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Check-In Status",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scan wristband to view guest details",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStartScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = nfcHandler != null && nfcHandler.isNfcAvailable()
            ) {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = "Scan",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Wristband")
            }

            if (nfcHandler == null || !nfcHandler.isNfcAvailable()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "NFC is not available. Please enable it in settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Powered by Footer at bottom
        PoweredByFooter(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun ScanningUI(onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NfcAnimatedIcon(isWriting = false)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Ready to Scan",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hold wristband near the back of your phone",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}

@Composable
fun GuestDataDisplay(
    guestData: WalletLookupGuestData,
    onScanAnother: () -> Unit
) {
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with name and balance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (demoMode) demoUsername else guestData.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${guestData.balance ?: 0} Points",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Basic Info Card
        item {
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
                    DetailRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = if (demoMode) demoEmail else guestData.email
                    )
                    HorizontalDivider()
                    DetailRow(
                        icon = Icons.Default.Badge,
                        label = "Type",
                        value = guestData.userType.replace("_", " ").capitalize()
                    )
                }
            }
        }

        // Earned Actions Section
        item {
            val earnedActions = guestData.earnedActions ?: emptyList()
            ExpandableSection(
                title = "Earned Points",
                count = earnedActions.size,
                icon = Icons.Default.TrendingUp,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                if (earnedActions.isEmpty()) {
                    Text(
                        text = "No points earned yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    earnedActions.forEach { action ->
                        EarnedActionItem(action)
                        if (action != earnedActions.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Redeemed Items Section
        item {
            val redeemedItems = guestData.redeemedItems ?: emptyList()
            ExpandableSection(
                title = "Redeemed Items",
                count = redeemedItems.size,
                icon = Icons.Default.ShoppingBag,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                if (redeemedItems.isEmpty()) {
                    Text(
                        text = "No items redeemed yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    redeemedItems.forEach { item ->
                        RedeemedItemCard(item)
                        if (item != redeemedItems.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Available Actions Section
        item {
            val availableActions = guestData.availableActions ?: emptyList()
            ExpandableSection(
                title = "Available to Earn",
                count = availableActions.size,
                icon = Icons.Default.Celebration,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (availableActions.isEmpty()) {
                    Text(
                        text = "All actions completed!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    availableActions.forEach { action ->
                        AvailableActionItem(action)
                        if (action != availableActions.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // Scan Another Button
        item {
            Button(
                onClick = onScanAnother,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = "Scan Another",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Another Wristband")
            }
        }

        // Powered by Footer
        item {
            PoweredByFooter(
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun EarnedActionItem(action: EarnedAction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action.actionName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDateTime(action.earnedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        ) {
            Text(
                text = "+${action.pointsEarned} pts",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun RedeemedItemCard(item: RedeemedItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.merchandiseName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDateTime(item.redeemedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        ) {
            Text(
                text = "-${item.pointsSpent} pts",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun AvailableActionItem(action: AvailableAction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = action.actionName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
        ) {
            Text(
                text = "${action.points} pts",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US)
        val date = inputFormat.parse(dateTimeString)
        date?.let { outputFormat.format(it) } ?: dateTimeString
    } catch (e: Exception) {
        dateTimeString
    }
}