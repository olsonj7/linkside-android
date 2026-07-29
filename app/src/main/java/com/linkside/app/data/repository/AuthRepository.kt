package com.linkside.app.data.repository

import com.linkside.app.data.api.ApiException
import com.linkside.app.data.api.LinksideApi
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.data.api.runApi
import com.linkside.app.data.auth.TokenStore
import com.linkside.app.data.model.EmailLoginRequest
import com.linkside.app.data.model.EmailRegisterRequest
import com.linkside.app.data.model.ForgotPasswordRequest
import com.linkside.app.data.model.GoogleAuthRequest
import com.linkside.app.data.model.SendCodeRequest
import com.linkside.app.data.model.LinkEmailRequest
import com.linkside.app.data.model.LinkPhoneRequest
import com.linkside.app.data.model.ResetPasswordRequest
import com.linkside.app.data.model.UpdateProfileRequest
import com.linkside.app.data.model.User
import com.linkside.app.data.model.VerifyCodeRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepository(
    private val api: LinksideApi,
    private val tokenStore: TokenStore,
) {
    fun hasStoredToken(): Boolean = !tokenStore.readToken().isNullOrBlank()

    suspend fun loadUser(): User? {
        if (!hasStoredToken()) return null
        val response = runApi { api.me() }
        if (!response.ok || response.user == null) {
            throw ApiException(response.error ?: "Failed to fetch user")
        }
        return response.user
    }

    suspend fun sendCode(phone: String) {
        val normalized = PhoneUtils.normalizePhone(phone)
        val response = runApi { api.sendCode(SendCodeRequest(normalized)) }
        if (!response.ok) {
            throw ApiException(response.error ?: response.message ?: "Send code failed")
        }
    }

    suspend fun verifyCode(phone: String, code: String): User {
        val normalized = PhoneUtils.normalizePhone(phone)
        val response = runApi { api.verifyCode(VerifyCodeRequest(normalized, code.trim())) }
        if (!response.ok) {
            throw ApiException(response.error ?: response.message ?: "Verify failed")
        }
        val token = response.token ?: throw ApiException("Missing auth token")
        tokenStore.saveToken(token)
        return loadUser() ?: throw ApiException("Failed to load user after verify")
    }

    suspend fun googleAuth(idToken: String): User {
        val response = runApi { api.googleAuth(GoogleAuthRequest(idToken)) }
        if (!response.ok) {
            throw ApiException(response.error ?: "Google auth failed")
        }
        val token = response.token ?: throw ApiException("Missing auth token")
        tokenStore.saveToken(token)
        return response.user ?: loadUser() ?: throw ApiException("Failed to load user after Google auth")
    }

    suspend fun emailLogin(email: String, password: String): User {
        val response = runApi {
            api.emailLogin(
                EmailLoginRequest(
                    email = email.trim().lowercase(),
                    password = password,
                ),
            )
        }
        if (!response.ok) {
            throw ApiException(response.error ?: "Login failed")
        }
        val token = response.token ?: throw ApiException("Missing auth token")
        tokenStore.saveToken(token)
        return response.user ?: loadUser() ?: throw ApiException("Failed to load user after email login")
    }

    suspend fun emailRegister(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        smsConsent: Boolean,
        phoneCode: String? = null,
    ): User {
        val response = runApi {
            api.emailRegister(
                EmailRegisterRequest(
                    email = email.trim().lowercase(),
                    password = password,
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    phone = PhoneUtils.normalizePhone(phone),
                    phoneCode = phoneCode?.trim()?.takeIf { it.isNotEmpty() },
                    smsConsent = smsConsent,
                ),
            )
        }
        if (!response.ok) {
            throw ApiException(response.error ?: "Registration failed")
        }
        val token = response.token ?: throw ApiException("Missing auth token")
        tokenStore.saveToken(token)
        return response.user ?: loadUser() ?: throw ApiException("Failed to load user after registration")
    }

    suspend fun updateProfile(firstName: String?, lastName: String?): User {
        return updateProfileExtended(firstName = firstName, lastName = lastName)
    }

    suspend fun updateProfileExtended(
        firstName: String? = null,
        lastName: String? = null,
        address: String? = null,
        city: String? = null,
        state: String? = null,
        zipCode: String? = null,
        handicap: Double? = null,
        clearHandicap: Boolean = false,
        favoriteCourses: List<com.linkside.app.data.model.FavoriteCourse>? = null,
    ): User {
        val body = buildMap<String, Any?> {
            firstName?.trim()?.takeIf { it.isNotEmpty() }?.let { put("firstName", it) }
            lastName?.trim()?.takeIf { it.isNotEmpty() }?.let { put("lastName", it) }
            address?.trim()?.takeIf { it.isNotEmpty() }?.let { put("address", it) }
            city?.trim()?.takeIf { it.isNotEmpty() }?.let { put("city", it) }
            state?.trim()?.takeIf { it.isNotEmpty() }?.let { put("state", it) }
            zipCode?.trim()?.takeIf { it.isNotEmpty() }?.let { put("zipCode", it) }
            when {
                clearHandicap -> put("handicap", null)
                handicap != null -> put("handicap", handicap)
            }
            favoriteCourses?.let { courses ->
                put(
                    "favoriteCourses",
                    courses.map { mapOf("placeId" to it.placeId, "name" to it.name, "address" to it.address) },
                )
            }
        }
        if (body.isEmpty()) {
            return loadUser() ?: throw ApiException("Failed to load user")
        }
        val response = runApi { api.patchProfile(body) }
        if (!response.ok || response.user == null) {
            throw ApiException(response.error ?: "Failed to update profile")
        }
        return response.user
    }

    suspend fun linkPhone(phone: String, code: String): User {
        val response = runApi {
            api.linkPhone(LinkPhoneRequest(PhoneUtils.normalizePhone(phone), code))
        }
        if (!response.ok || response.user == null) {
            throw ApiException(response.error ?: "Failed to link phone")
        }
        return response.user
    }

    suspend fun linkEmail(email: String, password: String): User {
        val response = runApi {
            api.linkEmail(
                LinkEmailRequest(
                    email = email.trim().lowercase(),
                    password = password,
                ),
            )
        }
        if (!response.ok || response.user == null) {
            throw ApiException(response.error ?: "Failed to link email")
        }
        return response.user
    }

    suspend fun linkGoogle(idToken: String): User {
        val response = runApi { api.linkGoogle(GoogleAuthRequest(idToken)) }
        if (!response.ok || response.user == null) {
            throw ApiException(response.error ?: "Failed to link Google")
        }
        return response.user
    }

    suspend fun forgotPassword(email: String) {
        val response = runApi {
            api.forgotPassword(ForgotPasswordRequest(email.trim().lowercase()))
        }
        if (!response.ok) {
            throw ApiException(response.error ?: response.message ?: "Failed to send reset code")
        }
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String) {
        val response = runApi {
            api.resetPassword(
                ResetPasswordRequest(
                    email = email.trim().lowercase(),
                    code = code.trim(),
                    newPassword = newPassword,
                ),
            )
        }
        if (!response.ok) {
            throw ApiException(response.error ?: response.message ?: "Failed to reset password")
        }
    }

    suspend fun uploadAvatar(imageBytes: ByteArray, mimeType: String = "image/jpeg"): User {
        val ext = if (mimeType.contains("png", ignoreCase = true)) "png" else "jpg"
        val body = imageBytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("photo", "avatar.$ext", body)
        val response = runApi { api.uploadAvatar(part) }
        if (!response.ok || response.user == null) {
            throw ApiException(response.error ?: "Failed to upload profile photo")
        }
        return response.user
    }

    suspend fun deleteAvatar(): User {
        val response = runApi { api.deleteAvatar() }
        if (!response.ok || response.user == null) {
            throw ApiException(response.error ?: "Failed to remove profile photo")
        }
        return response.user
    }

    fun signOut() {
        tokenStore.clearToken()
    }

    suspend fun deleteAccount() {
        val response = runApi { api.deleteAccount() }
        if (!response.ok) {
            throw ApiException(response.error ?: "Failed to delete account")
        }
        tokenStore.clearToken()
    }

    companion object {
        fun create(api: LinksideApi, tokenStore: TokenStore): AuthRepository =
            AuthRepository(api, tokenStore)
    }
}
