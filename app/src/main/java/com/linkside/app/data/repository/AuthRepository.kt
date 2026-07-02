package com.linkside.app.data.repository

import com.linkside.app.data.api.ApiException
import com.linkside.app.data.api.LinksideApi
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.data.api.runApi
import com.linkside.app.data.auth.TokenStore
import com.linkside.app.data.model.EmailLoginRequest
import com.linkside.app.data.model.EmailRegisterRequest
import com.linkside.app.data.model.GoogleAuthRequest
import com.linkside.app.data.model.SendCodeRequest
import com.linkside.app.data.model.UpdateProfileRequest
import com.linkside.app.data.model.User
import com.linkside.app.data.model.VerifyCodeRequest

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
    ): User {
        val response = runApi {
            api.emailRegister(
                EmailRegisterRequest(
                    email = email.trim().lowercase(),
                    password = password,
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    phone = PhoneUtils.normalizePhone(phone),
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

    fun signOut() {
        tokenStore.clearToken()
    }

    companion object {
        fun create(api: LinksideApi, tokenStore: TokenStore): AuthRepository =
            AuthRepository(api, tokenStore)
    }
}
