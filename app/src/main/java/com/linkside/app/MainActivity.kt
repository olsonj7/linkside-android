package com.linkside.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.linkside.app.data.prefs.ProfilePreferences
import com.linkside.app.ui.LinksideApp
import com.linkside.app.ui.theme.LinksideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val profilePreferences = ProfilePreferences(this)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by rememberSaveable {
                mutableStateOf(profilePreferences.prefersDarkMode ?: systemDark)
            }
            LinksideTheme(darkTheme = darkTheme) {
                LinksideApp(
                    onDarkModeChange = { enabled ->
                        darkTheme = enabled
                        profilePreferences.prefersDarkMode = enabled
                    },
                )
            }
        }
    }
}
