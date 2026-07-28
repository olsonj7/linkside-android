package com.linkside.app.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.linkside.app.BuildConfig

class TokenStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    init {
        // Drop sessions minted against a different backend (e.g. prod → dev switch).
        val currentApi = BuildConfig.API_BASE_URL.trimEnd('/')
        val storedApi = prefs.getString(KEY_API_BASE, null)
        if (storedApi != null && storedApi != currentApi) {
            prefs.edit()
                .remove(KEY_TOKEN)
                .putString(KEY_API_BASE, currentApi)
                .apply()
        } else if (storedApi == null) {
            prefs.edit().putString(KEY_API_BASE, currentApi).apply()
        }
    }

    fun readToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveToken(token: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_API_BASE, BuildConfig.API_BASE_URL.trimEnd('/'))
            .apply()
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    companion object {
        private const val PREFS_NAME = "linkside_secure_prefs"
        private const val KEY_TOKEN = "authToken"
        private const val KEY_API_BASE = "apiBaseUrl"
    }
}
