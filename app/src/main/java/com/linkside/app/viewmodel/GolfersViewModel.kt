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
    val isPreparingInvite: Boolean = false,
    val errorMessage: String? = null,
    val inviteError: String? = null,
)

class GolfersViewModel(
    private val repository: LinksideRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GolfersUiState())
    val uiState: StateFlow<GolfersUiState> = _uiState.asStateFlow()

    fun syncFromServer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val golfersResult = runCatching { repository.fetchSavedGolfers() }
            val groupsResult = runCatching { repository.fetchFriendGroups() }
            _uiState.update { state ->
                state.copy(
                    golfers = golfersResult.getOrDefault(state.golfers),
                    groups = groupsResult.getOrDefault(state.groups),
                    isLoading = false,
                    errorMessage = golfersResult.exceptionOrNull()?.message
                        ?: groupsResult.exceptionOrNull()?.message,
                )
            }
            if (golfersResult.isSuccess) {
                refreshContactStatuses()
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

    fun contactStatus(phone: String): ContactStatus? {
        val statuses = _uiState.value.contactStatuses
        val normalized = com.linkside.app.data.api.PhoneUtils.normalizePhone(phone)
        return statuses[normalized] ?: statuses[phone]
    }

    fun prepareAppInvite(
        friend: Friend,
        hostName: String?,
        onReady: (phone: String, message: String) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPreparingInvite = true, inviteError = null) }
            try {
                val invite = repository.getOptInMessage(
                    phone = friend.phone,
                    name = friend.fullName,
                    hostName = hostName,
                )
                _uiState.update { it.copy(isPreparingInvite = false) }
                onReady(invite.phone.ifBlank { friend.phone }, invite.message)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPreparingInvite = false,
                        inviteError = e.message
                            ?: "Failed to prepare invite message. Please check your connection and try again.",
                    )
                }
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
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Group name is required.") }
            return
        }
        if (hasDuplicateGroupName(trimmed)) {
            _uiState.update { it.copy(errorMessage = "A group named \"$trimmed\" already exists.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingGroup = true, errorMessage = null) }
            try {
                val group = repository.createFriendGroup(trimmed, members)
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
        val trimmed = group.name.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Group name is required.") }
            return
        }
        if (hasDuplicateGroupName(trimmed, excludingId = group.id)) {
            _uiState.update { it.copy(errorMessage = "A group named \"$trimmed\" already exists.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingGroup = true, errorMessage = null) }
            try {
                val updated = repository.updateFriendGroup(group.copy(name = trimmed))
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
        val id = group.id.trim()
        if (id.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Group not found") }
            return
        }
        val previous = _uiState.value.groups
        _uiState.update { it.copy(groups = it.groups.filterNot { g -> g.id == group.id }, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.deleteFriendGroup(id)
            } catch (e: Exception) {
                // Re-sync so the list matches the server if the local id was stale.
                try {
                    val groups = repository.fetchFriendGroups()
                    _uiState.update { it.copy(groups = groups, errorMessage = e.message) }
                } catch (_: Exception) {
                    _uiState.update { it.copy(groups = previous, errorMessage = e.message) }
                }
            }
        }
    }

    fun hasDuplicateGroupName(name: String, excludingId: String? = null): Boolean {
        val needle = name.trim().lowercase()
        if (needle.isEmpty()) return false
        return _uiState.value.groups.any { group ->
            group.name.trim().lowercase() == needle &&
                (excludingId == null || !group.id.equals(excludingId, ignoreCase = true))
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, inviteError = null) }
    }

    fun clearInviteError() {
        _uiState.update { it.copy(inviteError = null) }
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
