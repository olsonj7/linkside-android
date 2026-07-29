package com.linkside.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linkside.app.data.model.User
import com.linkside.app.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isInitializing: Boolean = true,
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
) {
    val isAuthenticated: Boolean get() = user != null
}

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(800)
            bootstrap()
        }
    }

    private suspend fun bootstrap() {
        if (!repository.hasStoredToken()) {
            _uiState.update { it.copy(isInitializing = false, user = null) }
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        try {
            val user = repository.loadUser()
            _uiState.update {
                it.copy(isInitializing = false, isLoading = false, user = user, errorMessage = null)
            }
        } catch (e: Exception) {
            repository.signOut()
            _uiState.update {
                it.copy(
                    isInitializing = false,
                    isLoading = false,
                    user = null,
                    errorMessage = e.message,
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun sendCode(phone: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.sendCode(phone)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    /** Phone OTP for email signup — does not toggle the global auth loading spinner. */
    fun sendPhoneVerificationCode(phone: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            try {
                repository.sendCode(phone)
                onComplete(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
                onComplete(false)
            }
        }
    }

    fun verifyCode(phone: String, code: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.verifyCode(phone, code)
                _uiState.update { it.copy(isLoading = false, user = user) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun googleAuth(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.googleAuth(idToken)
                _uiState.update { it.copy(isLoading = false, user = user) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun emailLogin(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.emailLogin(email, password)
                _uiState.update { it.copy(isLoading = false, user = user) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun emailRegister(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        smsConsent: Boolean,
        phoneCode: String? = null,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.emailRegister(
                    email,
                    password,
                    firstName,
                    lastName,
                    phone,
                    smsConsent,
                    phoneCode,
                )
                _uiState.update { it.copy(isLoading = false, user = user) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun forgotPassword(email: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.forgotPassword(email)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun resetPassword(email: String, code: String, newPassword: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.resetPassword(email, code, newPassword)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun linkEmail(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.linkEmail(email, password)
                _uiState.update { it.copy(isLoading = false, user = user) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun linkGoogle(idToken: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.linkGoogle(idToken)
                _uiState.update { it.copy(isLoading = false, user = user) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.updateProfile(firstName, lastName)
                _uiState.update { it.copy(isLoading = false, user = user) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateProfileExtended(
        firstName: String,
        lastName: String,
        address: String,
        city: String,
        state: String,
        zipCode: String,
        handicapText: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val trimmedHandicap = handicapText.trim()
                val handicap = trimmedHandicap.takeIf { it.isNotEmpty() }?.toDoubleOrNull()
                    ?: if (trimmedHandicap.isEmpty()) null else throw IllegalArgumentException("Enter a valid handicap index")
                val user = repository.updateProfileExtended(
                    firstName = firstName,
                    lastName = lastName,
                    address = address,
                    city = city,
                    state = state,
                    zipCode = zipCode,
                    handicap = handicap,
                    clearHandicap = trimmedHandicap.isEmpty(),
                )
                _uiState.update { it.copy(isLoading = false, user = user) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateFavoriteCourses(courses: List<com.linkside.app.data.model.FavoriteCourse>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.updateProfileExtended(favoriteCourses = courses)
                _uiState.update { it.copy(isLoading = false, user = user) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun uploadAvatar(imageBytes: ByteArray, mimeType: String = "image/jpeg", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, errorMessage = null) }
            try {
                val user = repository.uploadAvatar(imageBytes, mimeType)
                _uiState.update { it.copy(isUploadingAvatar = false, user = user) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploadingAvatar = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteAvatar(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, errorMessage = null) }
            try {
                val user = repository.deleteAvatar()
                _uiState.update { it.copy(isUploadingAvatar = false, user = user) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploadingAvatar = false, errorMessage = e.message) }
            }
        }
    }

    fun removeFavoriteCourse(placeId: String) {
        val current = _uiState.value.user?.favoriteCourses.orEmpty()
        updateFavoriteCourses(current.filter { it.placeId != placeId })
    }

    fun addFavoriteCourse(course: com.linkside.app.data.model.GolfCourse) {
        val current = _uiState.value.user?.favoriteCourses.orEmpty()
        if (current.any { it.placeId == course.placeId }) return
        val updated = current + com.linkside.app.data.model.FavoriteCourse(
            placeId = course.placeId,
            name = course.name,
            address = course.address,
        )
        updateFavoriteCourses(updated)
    }

    fun toggleFavoriteCourse(course: com.linkside.app.data.model.GolfCourse) {
        val current = _uiState.value.user?.favoriteCourses.orEmpty()
        if (current.any { it.placeId == course.placeId }) {
            removeFavoriteCourse(course.placeId)
        } else {
            addFavoriteCourse(course)
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            try {
                val user = repository.loadUser()
                _uiState.update { it.copy(user = user) }
            } catch (_: Exception) {
                // ignore refresh failures on profile tab
            }
        }
    }

    fun linkPhone(phone: String, code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = repository.linkPhone(phone, code)
                _uiState.update { it.copy(user = user, isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.update { AuthUiState(isInitializing = false) }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.deleteAccount()
                _uiState.update { AuthUiState(isInitializing = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                            ?: "Could not delete account. Please try again or contact support@getlinkside.com.",
                    )
                }
            }
        }
    }

    class Factory(
        private val repository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
