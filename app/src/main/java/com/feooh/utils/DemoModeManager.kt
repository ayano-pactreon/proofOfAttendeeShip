package com.feooh.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

object DemoModeManager {
    private const val PREF_NAME = "subzero_prefs"
    private const val KEY_DEMO_MODE = "demo_mode_enabled"
    private const val KEY_DEMO_USERNAME = "demo_username"
    private const val KEY_DEMO_EMAIL = "demo_email"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isDemoModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DEMO_MODE, false)
    }

    fun getDemoUsername(context: Context): String {
        return getPrefs(context).getString(KEY_DEMO_USERNAME, "Demo User") ?: "Demo User"
    }

    fun getDemoEmail(context: Context): String {
        return getPrefs(context).getString(KEY_DEMO_EMAIL, "demo@example.com") ?: "demo@example.com"
    }

    fun setDemoMode(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_DEMO_MODE, enabled)
            .apply()
    }

    fun setDemoCredentials(context: Context, username: String, email: String) {
        getPrefs(context).edit()
            .putString(KEY_DEMO_USERNAME, username)
            .putString(KEY_DEMO_EMAIL, email)
            .apply()
    }

    fun resetToDefault(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_DEMO_MODE)
            .remove(KEY_DEMO_USERNAME)
            .remove(KEY_DEMO_EMAIL)
            .apply()
    }
}

/**
 * Composable helper to check if demo mode is enabled
 * Note: This reads fresh from SharedPreferences on each recomposition
 */
@Composable
fun isDemoMode(): Boolean {
    val context = LocalContext.current
    // Don't use remember so it reads fresh value on each recomposition
    return DemoModeManager.isDemoModeEnabled(context)
}