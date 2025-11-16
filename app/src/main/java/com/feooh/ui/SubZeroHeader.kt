package com.feooh.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.feooh.R

/**
 * Reusable SubZero header component with Polkadot and SubZero logos
 * Displays at the top of screens
 * Easter egg: Tap 10 times quickly to open developer options
 */
@Composable
fun SubZeroHeader(
    modifier: Modifier = Modifier,
    onEasterEggTriggered: () -> Unit = {}
) {
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null // No ripple effect
            ) {
                val currentTime = System.currentTimeMillis()

                // Reset tap count if more than 2 seconds have passed since last tap
                if (currentTime - lastTapTime > 2000) {
                    tapCount = 0
                }

                lastTapTime = currentTime
                tapCount++

                // Trigger easter egg after 10 taps
                if (tapCount >= 10) {
                    onEasterEggTriggered()
                    tapCount = 0
                    lastTapTime = 0L
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.polkadot_logo),
            contentDescription = "Polkadot",
            modifier = Modifier
                .height(40.dp)
                .padding(bottom = 8.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.subzero_logo),
            contentDescription = "SubZero",
            modifier = Modifier.height(120.dp)
        )
    }
}
