package com.feooh

import android.app.ComponentCaller
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feooh.ui.theme.SubZeroTheme





import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.feooh.ui.theme.SubZeroPurple
import com.feooh.ui.theme.SubZeroPurpleDark
import kotlinx.coroutines.launch

/**
 * The main activity of the application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubZeroTheme {
                MainTabScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE // Required on Android 12 +
        )
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }
}

/**
 * A composable function that displays a logo and a greeting message.
 *
 * @param name The name to display in the greeting message.
 * @param modifier The modifier to apply to the composable.
 */
@Composable
fun GreetingWithLogo(name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Step 1: Add the Image composable for the logo
        Image(
            painter = painterResource(id = R.drawable.logo), // <-- Replace with your logo's file name
            contentDescription = "Application Logo", // A description for accessibility
            modifier = Modifier.size(120.dp) // Adjust the size as needed
        )

        // Step 2: The original Text composable
        Text(
            text = "Hello $name!",
            modifier = Modifier.padding(top = 16.dp) // Add some space between the logo and text
        )
    }
}

/**
 * A preview of the [GreetingWithLogo] composable.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SubZeroTheme {
        GreetingWithLogo("Android")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabScreen() {
    val tabs = TabScreen.entries.toTypedArray()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SubZeroPurpleDark,
                contentColor = Color.White,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                tab.title,
                                color = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = SubZeroPurple
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // HorizontalPager holds the content for each tab
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            // The content of the page is determined by the `page` index
            when (page) {
                0 -> RegistrationScreen()
                1 -> StatusScreen()
                2 -> MerchandiseScreen()
                3 -> AdminScreen()
            }
        }
    }
}
