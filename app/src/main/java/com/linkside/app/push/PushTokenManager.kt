package com.linkside.app.push

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.linkside.app.data.repository.LinksideRepository
import kotlinx.coroutines.tasks.await

object PushTokenManager {
    private const val TAG = "PushToken"
    private const val PREFS = "push_tokens"
    private const val KEY_FCM = "fcm_token"

    fun cachedToken(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FCM, null)

    private fun cacheToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM, token)
            .apply()
    }

    fun clearCache(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    suspend fun fetchToken(context: Context): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            cacheToken(context, token)
            token
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch FCM token: ${e.message}")
            cachedToken(context)
        }
    }

    /** Upload the current FCM token to the backend (best-effort). */
    suspend fun syncWithServer(context: Context, repository: LinksideRepository) {
        val token = fetchToken(context) ?: return
        try {
            repository.registerDeviceToken(token)
            Log.i(TAG, "Device token registered")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register device token: ${e.message}")
        }
    }

    suspend fun unregisterFromServer(repository: LinksideRepository) {
        try {
            repository.unregisterDeviceToken()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister device token: ${e.message}")
        }
    }
}
