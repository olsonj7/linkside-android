package com.linkside.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linkside.app.data.model.GolfCourse
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.User
import com.linkside.app.data.repository.LinksideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class TeeTimeUiState(
    val teeTimes: List<TeeTime> = emptyList(),
    val courseSearchResults: List<GolfCourse> = emptyList(),
    val isSearchingCourses: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class TeeTimeViewModel(
    private val repository: LinksideRepository,
) : ViewModel() {
    var currentUser: User? = null

    private val _uiState = MutableStateFlow(TeeTimeUiState())
    val uiState: StateFlow<TeeTimeUiState> = _uiState.asStateFlow()

    fun loadTeeTimes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val all = repository.fetchTeeTimes()
                val user = currentUser
                val filtered = all.filter { !it.isDeclinedBy(user) || it.isActiveDeclined(user) }
                _uiState.update { it.copy(teeTimes = filtered, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun createTeeTime(
        courseName: String,
        courseId: String?,
        date: Instant,
        golfersNeeded: Int,
        invites: List<com.linkside.app.data.model.Friend>,
        timeMode: String = "specific",
        timeWindows: List<String> = emptyList(),
        playFormat: String? = null,
        greenFee: Double? = null,
        onSuccess: (TeeTime) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val created = repository.createTeeTime(
                    courseName,
                    courseId,
                    date,
                    golfersNeeded,
                    invites,
                    timeMode,
                    timeWindows,
                    playFormat,
                    greenFee,
                )
                _uiState.update { it.copy(teeTimes = listOf(created) + it.teeTimes, isLoading = false) }
                onSuccess(created)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateRsvp(teeTimeId: String, phone: String?, status: InviteStatus) {
        viewModelScope.launch {
            try {
                val updated = repository.updateInviteStatus(teeTimeId, phone, status.raw)
                _uiState.update { state ->
                    state.copy(teeTimes = state.teeTimes.map { if (it.id == teeTimeId) updated else it })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun refreshTeeTime(id: String, onLoaded: (TeeTime) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val updated = repository.fetchTeeTime(id)
                _uiState.update { state ->
                    state.copy(teeTimes = state.teeTimes.map { if (it.id == id) updated else it })
                }
                onLoaded(updated)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun searchCourses(query: String) {
        if (query.length < 2) {
            clearCourseSearch()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingCourses = true) }
            try {
                val results = repository.searchCourses(query)
                _uiState.update { it.copy(courseSearchResults = results, isSearchingCourses = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(courseSearchResults = emptyList(), isSearchingCourses = false) }
            }
        }
    }

    fun clearCourseSearch() {
        _uiState.update { it.copy(courseSearchResults = emptyList(), isSearchingCourses = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun nextUpTeeTime(user: User?): TeeTime? {
        val now = Instant.now()
        return _uiState.value.teeTimes.firstOrNull { teeTime ->
            val date = teeTime.parsedInstant() ?: return@firstOrNull false
            date.isAfter(now) &&
                date.isBefore(now.plusSeconds(48 * 3600)) &&
                !teeTime.isDeclinedBy(user)
        }
    }

    class Factory(
        private val repository: LinksideRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TeeTimeViewModel(repository) as T
    }
}

private fun TeeTime.isActiveDeclined(user: User?): Boolean {
    if (!isDeclinedBy(user)) return false
    val date = parsedInstant() ?: return false
    return date.isAfter(Instant.now().minusSeconds(24 * 3600))
}
