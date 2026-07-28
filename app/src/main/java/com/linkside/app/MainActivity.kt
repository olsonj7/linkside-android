package com.linkside.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.linkside.app.data.prefs.ProfilePreferences
import com.linkside.app.push.PushIntentParser
import com.linkside.app.push.PushNotificationHelper
import com.linkside.app.push.PushRouter
import com.linkside.app.ui.LinksideApp
import com.linkside.app.ui.theme.LinksideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PushNotificationHelper.ensureChannel(this)
        PushIntentParser.parse(intent)?.let { PushRouter.publish(it) }
        enableEdgeToEdge()
        val profilePreferences = ProfilePreferences(this)
        setContent {
            // Brand default is dark (matches iOS); honor an explicit user preference when set.
            var darkTheme by rememberSaveable {
                mutableStateOf(profilePreferences.prefersDarkMode ?: true)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PushIntentParser.parse(intent)?.let { PushRouter.publish(it) }
    }
}
