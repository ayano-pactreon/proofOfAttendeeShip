package com.feooh

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.feooh.data.model.EarnAction
import com.feooh.data.model.MerchandiseItem
import com.feooh.nfc.NfcHandler
import com.feooh.ui.PoweredByFooter
import com.feooh.ui.SubZeroHeader
import com.feooh.viewmodel.MerchandiseUiState
import com.feooh.viewmodel.MerchandiseViewModel
import kotlinx.coroutines.launch

/**
 * Merchandise screen with NFC-based redemption and earning
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MerchandiseScreen(viewModel: MerchandiseViewModel = viewModel()) {
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
                    text = "Merchandise Access",
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

    val context = LocalContext.current
    val activity = context as? Activity

    val merchandise by viewModel.merchandise.collectAsState()
    val earnActions by viewModel.earnActions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedMerch by remember { mutableStateOf<MerchandiseItem?>(null) }
    var selectedAction by remember { mutableStateOf<EarnAction?>(null) }
    var showRedeemDialog by remember { mutableStateOf(false) }
    var showEarnDialog by remember { mutableStateOf(false) }
    var nfcHandler by remember { mutableStateOf<NfcHandler?>(null) }

    // Tab state
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // Initialize NFC handler
    LaunchedEffect(activity) {
        activity?.let {
            nfcHandler = NfcHandler(it)
        }
    }

    // Cleanup NFC handler
    DisposableEffect(Unit) {
        onDispose {
            nfcHandler?.disable()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Merchandise",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                when (pagerState.currentPage) {
                    0 -> viewModel.loadMerchandise()
                    1 -> viewModel.loadEarnActions()
                }
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Redeem") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("Earn") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status message
        when (val state = uiState) {
            is MerchandiseUiState.Loading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    state.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            is MerchandiseUiState.Success -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            state.message,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.resetUiState() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }
            is MerchandiseUiState.Error -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            state.message,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.resetUiState() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }
            else -> {}
        }

        // Horizontal Pager for tab content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> RedeemTab(
                    merchandise = merchandise,
                    onItemClick = { item ->
                        selectedMerch = item
                        showRedeemDialog = true
                    },
                    onLoadData = { viewModel.loadMerchandise() }
                )
                1 -> EarnTab(
                    earnActions = earnActions,
                    onActionClick = { action ->
                        selectedAction = action
                        showEarnDialog = true
                    },
                    onLoadData = { viewModel.loadEarnActions() }
                )
            }
        }
    }

    // Redeem NFC Scan Dialog
    if (showRedeemDialog && selectedMerch != null) {
        RedeemNfcDialog(
            item = selectedMerch!!,
            nfcHandler = nfcHandler,
            onDismiss = {
                showRedeemDialog = false
                nfcHandler?.disable()
            },
            onWalletRead = { walletAddress ->
                showRedeemDialog = false
                nfcHandler?.disable()
                viewModel.redeemMerchandise(walletAddress, selectedMerch!!.code)
            }
        )
    }

    // Earn NFC Scan Dialog
    if (showEarnDialog && selectedAction != null) {
        EarnNfcDialog(
            action = selectedAction!!,
            nfcHandler = nfcHandler,
            onDismiss = {
                showEarnDialog = false
                nfcHandler?.disable()
            },
            onWalletRead = { walletAddress ->
                showEarnDialog = false
                nfcHandler?.disable()
                viewModel.earnPoints(walletAddress, selectedAction!!.code)
            }
        )
    }
}

/**
 * Redeem tab content showing merchandise list
 */
@Composable
fun RedeemTab(
    merchandise: List<MerchandiseItem>,
    onItemClick: (MerchandiseItem) -> Unit,
    onLoadData: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoadData()
    }

    if (merchandise.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No merchandise available",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(merchandise) { item ->
                MerchandiseCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

/**
 * Earn tab content showing earn actions list
 */
@Composable
fun EarnTab(
    earnActions: List<EarnAction>,
    onActionClick: (EarnAction) -> Unit,
    onLoadData: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoadData()
    }

    if (earnActions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No earn actions available",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(earnActions) { action ->
                EarnActionCard(
                    action = action,
                    onClick = { onActionClick(action) }
                )
            }
        }
    }
}

/**
 * Card displaying a merchandise item
 */
@Composable
fun MerchandiseCard(
    item: MerchandiseItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.code,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${item.pointsCost} pts",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Card displaying an earn action item
 */
@Composable
fun EarnActionCard(
    action: EarnAction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = action.code,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "+${action.points} pts",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/**
 * Dialog for NFC scanning - Redeem flow
 */
@Composable
fun RedeemNfcDialog(
    item: MerchandiseItem,
    nfcHandler: NfcHandler?,
    onDismiss: () -> Unit,
    onWalletRead: (String) -> Unit
) {
    var scanStatus by remember { mutableStateOf("Ready to scan") }
    var isScanning by remember { mutableStateOf(false) }

    LaunchedEffect(nfcHandler) {
        if (nfcHandler != null && !isScanning) {
            isScanning = true
            scanStatus = "Hold wristband near phone..."

            nfcHandler.enableReading(
                onTagRead = { walletAddress ->
                    scanStatus = "Wallet address read!"
                    onWalletRead(walletAddress)
                },
                onError = { error ->
                    scanStatus = "Error: $error"
                }
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Redeem ${item.name}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cost: ${item.pointsCost} points",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // NFC Animation/Icon placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(60.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83D\uDCF1", // Phone emoji
                        fontSize = 48.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = scanStatus,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Dialog for NFC scanning - Earn flow
 */
@Composable
fun EarnNfcDialog(
    action: EarnAction,
    nfcHandler: NfcHandler?,
    onDismiss: () -> Unit,
    onWalletRead: (String) -> Unit
) {
    var scanStatus by remember { mutableStateOf("Ready to scan") }
    var isScanning by remember { mutableStateOf(false) }

    LaunchedEffect(nfcHandler) {
        if (nfcHandler != null && !isScanning) {
            isScanning = true
            scanStatus = "Hold wristband near phone..."

            nfcHandler.enableReading(
                onTagRead = { walletAddress ->
                    scanStatus = "Wallet address read!"
                    onWalletRead(walletAddress)
                },
                onError = { error ->
                    scanStatus = "Error: $error"
                }
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Earn Points",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = action.name,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Reward: ${action.points} points",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // NFC Animation/Icon placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(60.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83D\uDCF1", // Phone emoji
                        fontSize = 48.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = scanStatus,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
