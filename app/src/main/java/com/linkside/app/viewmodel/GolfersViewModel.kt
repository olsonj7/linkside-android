package com.linkside.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linkside.app.data.model.ContactStatus
import com.linkside.app.data.model.Friend
import com.linkside.app.data.model.FriendGroup
import com.linkside.app.data.model.User
import com.linkside.app.data.repository.LinksideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GolfersUiState(
    val golfers: List<Friend> = emptyList(),
    val groups: List<FriendGroup> = emptyList(),
    val deviceContacts: List<Friend> = emptyList(),
    val contactStatuses: Map<String, ContactStatus> = emptyMap(),
    val isLoading: Boolean = false,
    val isSavingGroup: Boolean = false,
    val errorMessage: String? = null,
)

class GolfersViewModel(
    private val repository: LinksideRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GolfersUiState())
    val uiState: StateFlow<GolfersUiState> = _uiState.asStateFlow()

    fun syncFromServer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val golfers = repository.fetchSavedGolfers()
                val groups = repository.fetchFriendGroups()
                _uiState.update { it.copy(golfers = golfers, groups = groups, isLoading = false) }
                refreshContactStatuses()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadDeviceContacts(contacts: List<Friend>) {
        _uiState.update { it.copy(deviceContacts = contacts) }
    }

    fun refreshContactStatuses() {
        viewModelScope.launch {
            val phones = (_uiState.value.golfers + _uiState.value.deviceContacts).map { it.phone }
            try {
                val statuses = repository.checkContactStatuses(phones.distinct())
                _uiState.update { it.copy(contactStatuses = statuses) }
            } catch (_: Exception) {
                // Non-critical
            }
        }
    }

    fun saveGolfers(golfers: List<Friend>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.saveGolfers(golfers)
                _uiState.update { it.copy(golfers = golfers.sortedBy { f -> f.fullName.lowercase() }, isLoading = false) }
                refreshContactStatuses()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun addManualGolfer(friend: Friend) {
        val normalized = friend.copy(phone = com.linkside.app.data.api.PhoneUtils.normalizePhone(friend.phone))
        val current = _uiState.value.golfers
        if (current.any { it.phone == normalized.phone }) return
        saveGolfers(current + normalized)
    }

    fun removeGolfer(friend: Friend) {
        saveGolfers(_uiState.value.golfers.filterNot { it.phone == friend.phone })
    }

    fun createGroup(name: String, members: List<Friend>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingGroup = true, errorMessage = null) }
            try {
                val group = repository.createFriendGroup(name, members)
                _uiState.update {
                    it.copy(groups = it.groups + group, isSavingGroup = false)
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingGroup = false, errorMessage = e.message) }
            }
        }
    }

    fun updateGroup(group: FriendGroup, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingGroup = true, errorMessage = null) }
            try {
                val updated = repository.updateFriendGroup(group)
                _uiState.update { state ->
                    state.copy(
                        groups = state.groups.map { if (it.id == updated.id) updated else it },
                        isSavingGroup = false,
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingGroup = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteGroup(group: FriendGroup) {
        val previous = _uiState.value.groups
        _uiState.update { it.copy(groups = it.groups.filterNot { g -> g.id == group.id }) }
        viewModelScope.launch {
            try {
                repository.deleteFriendGroup(group.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(groups = previous, errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearLocalData() {
        _uiState.value = GolfersUiState()
    }

    fun canCreateGroup(user: User?): Boolean {
        if (user?.isSilver == true) return true
        return _uiState.value.groups.size < 3
    }

    class Factory(
        private val repository: LinksideRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GolfersViewModel(repository) as T
    }
}
