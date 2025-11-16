package com.feooh.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feooh.R

/**
 * Reusable "Powered by Feooh" footer component
 * Displays at the bottom of screens
 * Clicking opens feooh.com in browser
 */
@Composable
fun PoweredByFooter(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null // No ripple effect
            ) {
                // Open feooh.com in browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://feooh.com"))
                context.startActivity(intent)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "powered by",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = painterResource(id = R.drawable.feooh),
            contentDescription = "Feooh",
            modifier = Modifier.height(48.dp)
        )
    }
}
