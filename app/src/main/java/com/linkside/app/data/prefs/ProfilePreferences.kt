package com.linkside.app.data.prefs

import android.content.Context

class ProfilePreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var defaultGroupSize: Int
        get() = prefs.getInt(KEY_GROUP_SIZE, 4).coerceIn(2, 8)
        set(value) {
            prefs.edit().putInt(KEY_GROUP_SIZE, value.coerceIn(2, 8)).apply()
        }

    var prefersDarkMode: Boolean?
        get() = if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, true) else null
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_DARK_MODE) else putBoolean(KEY_DARK_MODE, value)
            }.apply()
        }

    var smsNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_SMS, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SMS, value).apply()
        }

    var pushNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PUSH, true)
        set(value) {
            prefs.edit().putBoolean(KEY_PUSH, value).apply()
        }

    companion object {
        private const val PREFS_NAME = "linkside_profile_prefs"
        private const val KEY_GROUP_SIZE = "default_group_size"
        private const val KEY_DARK_MODE = "prefers_dark_mode"
        private const val KEY_SMS = "sms_notifications"
        private const val KEY_PUSH = "push_notifications"
    }
}
