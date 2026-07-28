package com.linkside.app.data.api

class ApiException(message: String) : Exception(message)

object PhoneUtils {
    fun normalizePhone(phone: String): String {
        val trimmed = phone.trim()
        val digits = trimmed.filter { it.isDigit() }
        if (trimmed.startsWith("+")) {
            return "+$digits"
        }
        if (digits.length == 11 && digits.startsWith("1")) {
            return "+$digits"
        }
        return "+1$digits"
    }

    fun isValidPhone(phone: String): Boolean {
        val trimmed = phone.trim()
        val digits = trimmed.filter { it.isDigit() }
        return if (trimmed.startsWith("+")) {
            digits.length in 7..15
        } else {
            digits.length == 10
        }
    }

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        val regex = Regex("^[A-Z0-9a-z._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
        return regex.matches(trimmed)
    }
}

fun parseApiError(body: String?): String {
    if (body.isNullOrBlank()) return "Server error"
    val trimmed = body.trim()
    // Proxy / platform error pages (404, 502, etc.) come back as HTML — never
    // surface raw markup to the user; show a friendly generic message instead.
    if (trimmed.startsWith("<") ||
        trimmed.contains("<!doctype", ignoreCase = true) ||
        trimmed.contains("<html", ignoreCase = true)
    ) {
        return "Something went wrong. Please try again."
    }
    return try {
        val json = org.json.JSONObject(trimmed)
        json.optString("error").takeIf { it.isNotBlank() }
            ?: json.optString("message").takeIf { it.isNotBlank() }
            ?: "Server error"
    } catch (_: Exception) {
        // Non-JSON, non-HTML body — cap length so we don't dump a huge blob.
        trimmed.take(140)
    }
}
