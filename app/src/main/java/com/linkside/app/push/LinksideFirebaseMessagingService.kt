package com.linkside.app.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.linkside.app.LinksideApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LinksideFirebaseMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Log.i(TAG, "FCM token refreshed")
        PushTokenManager.clearCache(this)
        val app = applicationContext as? LinksideApplication ?: return
        scope.launch {
            PushTokenManager.syncWithServer(applicationContext, app.linksideRepository)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        PushNotificationHelper.showFromRemoteMessage(this, message)
    }

    companion object {
        private const val TAG = "FCM"
    }
}
