package com.feooh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

enum class TabScreen(
    val title: String,
    val icon: ImageVector
) {
    Registration("Register", Icons.Default.QrCodeScanner),
    Status("Status", Icons.Default.Nfc),
    Merchandise("Merch", Icons.Default.ShoppingBag),
    Admin("Admin", Icons.Default.Settings)
}
