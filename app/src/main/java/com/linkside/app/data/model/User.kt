package com.linkside.app.data.model

import com.squareup.moshi.Json

data class User(
    val id: String,
    val phone: String? = null,
    @Json(name = "createdAt") val createdAt: Double? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val handicap: Double? = null,
    val favoriteCourses: List<FavoriteCourse>? = null,
    val appleId: String? = null,
    val googleId: String? = null,
    val tier: String? = null,
    val avatarUrl: String? = null,
) {
    val isSilver: Boolean
        get() = tier == "silver" || tier == "gold"

    val isGold: Boolean
        get() = tier == "gold"

    val displayName: String
        get() {
            val parts = listOfNotNull(firstName, lastName)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            if (parts.isNotEmpty()) return parts.joinToString(" ")
            if (!phone.isNullOrBlank()) return phone
            if (!email.isNullOrBlank()) return email
            return "Linkside User"
        }

    val needsNameEntry: Boolean
        get() = firstName?.trim().orEmpty().isEmpty() ||
            lastName?.trim().orEmpty().isEmpty()

    val needsPhoneEntry: Boolean
        get() {
            if (!phone.isNullOrBlank()) return false
            return !googleId.isNullOrBlank() || !appleId.isNullOrBlank() || !email.isNullOrBlank()
        }
}

data class FavoriteCourse(
    val placeId: String,
    val name: String,
    val address: String? = null,
)

data class OkResponse(
    val ok: Boolean,
    val message: String? = null,
    val error: String? = null,
    val token: String? = null,
)

data class AuthResponse(
    val ok: Boolean,
    val token: String? = null,
    val user: User? = null,
    val error: String? = null,
)

data class UserResponse(
    val ok: Boolean,
    val user: User? = null,
    val error: String? = null,
)

data class SendCodeRequest(val phone: String)

data class VerifyCodeRequest(val phone: String, val code: String)

data class GoogleAuthRequest(@Json(name = "idToken") val idToken: String)

data class EmailLoginRequest(
    val email: String,
    val password: String,
)

data class ForgotPasswordRequest(val email: String)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String,
)

data class LinkEmailRequest(
    val email: String,
    val password: String,
)

data class EmailRegisterRequest(
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String,
    val phoneCode: String? = null,
    val smsConsent: Boolean = true,
)

data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    @Json(name = "zipCode") val zipCode: String? = null,
    val handicap: Double? = null,
    val favoriteCourses: List<FavoriteCourse>? = null,
)
