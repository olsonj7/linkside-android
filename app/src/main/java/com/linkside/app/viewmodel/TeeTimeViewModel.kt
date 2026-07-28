package com.linkside.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linkside.app.data.model.GolfCourse
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.data.model.Photo
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TeeTimeChatMessage
import com.linkside.app.data.model.TeeTimeScorecard
import com.linkside.app.data.model.User
import com.linkside.app.data.repository.LinksideRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant

data class TeeTimeUiState(
    val teeTimes: List<TeeTime> = emptyList(),
    val courseSearchResults: List<GolfCourse> = emptyList(),
    val isSearchingCourses: Boolean = false,
    val isLoading: Boolean = false,
    val teeTimePhotos: Map<String, List<Photo>> = emptyMap(),
    val teeTimeMessages: Map<String, List<TeeTimeChatMessage>> = emptyMap(),
    val roundScores: Map<String, Int> = emptyMap(),
    val teeTimeScorecards: Map<String, List<TeeTimeScorecard>> = emptyMap(),
    /** placeId → course website URL (from Google Places). */
    val courseWebsites: Map<String, String> = emptyMap(),
    val roundSummaries: Map<String, com.linkside.app.data.model.RoundSummary> = emptyMap(),
    val isLoadingRoundSummary: Boolean = false,
    val isSendingMessage: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isSavingScore: Boolean = false,
    val errorMessage: String? = null,
)

class TeeTimeViewModel(
    private val repository: LinksideRepository,
) : ViewModel() {
    var currentUser: User? = null

    private val _uiState = MutableStateFlow(TeeTimeUiState())
    val uiState: StateFlow<TeeTimeUiState> = _uiState.asStateFlow()

    private var chatPollJob: Job? = null

    fun loadTeeTimes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val all = repository.fetchTeeTimes()
                val user = currentUser
                val filtered = all.filter { !it.isDeclinedBy(user) || it.isActiveDeclined(user) }
                val scores = repository.fetchRoundScores()
                _uiState.update { it.copy(teeTimes = filtered, roundScores = scores, isLoading = false) }
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
        holesCount: Int = 18,
        roundName: String? = null,
        sendInvites: Boolean = true,
        onSuccess: (TeeTime, List<com.linkside.app.data.model.ManualInvite>) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = repository.createTeeTime(
                    courseName,
                    courseId,
                    date,
                    golfersNeeded,
                    invites,
                    timeMode,
                    timeWindows,
                    playFormat,
                    greenFee,
                    holesCount,
                    roundName,
                    sendInvites,
                )
                val created = result.teeTime!!
                _uiState.update { it.copy(teeTimes = listOf(created) + it.teeTimes, isLoading = false) }
                onSuccess(created, result.manualInvites)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateTeeTime(
        id: String,
        date: Instant,
        golfersNeeded: Int,
        timeMode: String,
        timeWindows: List<String>,
        playFormat: String?,
        greenFee: Double?,
        holesCount: Int? = null,
        roundName: String? = null,
        onSuccess: (TeeTime) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val updated = repository.updateTeeTime(
                    id = id,
                    date = date,
                    golfersNeeded = golfersNeeded,
                    timeMode = timeMode,
                    timeWindows = timeWindows,
                    playFormat = playFormat,
                    greenFee = greenFee,
                    holesCount = holesCount,
                    roundName = roundName,
                )
                _uiState.update { state ->
                    state.copy(
                        teeTimes = state.teeTimes.map { if (it.id == id) updated else it },
                        isLoading = false,
                    )
                }
                onSuccess(updated)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateRsvp(
        teeTimeId: String,
        phone: String?,
        status: InviteStatus,
        userId: String? = currentUser?.id,
    ) {
        // Optimistic update so Home/Profile react immediately (declined list, hide from upcoming).
        val user = currentUser
        _uiState.update { state ->
            state.copy(
                teeTimes = state.teeTimes.map { teeTime ->
                    if (teeTime.id != teeTimeId) teeTime
                    else teeTime.withInviteStatusFor(user = user, phone = phone, userId = userId, status = status)
                },
            )
        }
        viewModelScope.launch {
            try {
                val updated = repository.updateInviteStatus(
                    teeTimeId = teeTimeId,
                    phone = phone,
                    status = status.raw,
                    userId = userId,
                )
                _uiState.update { state ->
                    state.copy(teeTimes = state.teeTimes.map { if (it.id == teeTimeId) updated else it })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
                // Re-sync so a failed RSVP doesn't leave stale optimistic state.
                loadTeeTimes()
            }
        }
    }

    fun deleteTeeTime(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.deleteTeeTime(id)
                _uiState.update { state ->
                    state.copy(
                        teeTimes = state.teeTimes.filterNot { it.id == id },
                        isLoading = false,
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun addInvites(
        teeTimeId: String,
        invites: List<com.linkside.app.data.model.Friend>,
        notify: Boolean = true,
        onSuccess: (List<com.linkside.app.data.model.ManualInvite>) -> Unit = {},
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = repository.addTeeTimeInvites(teeTimeId, invites, notify = notify)
                val updated = result.teeTime
                if (updated != null) {
                    _uiState.update { state ->
                        state.copy(
                            teeTimes = state.teeTimes.map { if (it.id == teeTimeId) updated else it },
                            isLoading = false,
                        )
                    }
                } else {
                    refreshTeeTime(teeTimeId)
                    _uiState.update { it.copy(isLoading = false) }
                }
                onSuccess(result.manualInvites)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    /** Send invites for golfers saved via "Save without inviting". */
    fun sendPendingInvites(
        teeTimeId: String,
        onSuccess: (List<com.linkside.app.data.model.ManualInvite>) -> Unit = {},
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = repository.sendPendingInvites(teeTimeId)
                val updated = result.teeTime!!
                _uiState.update { state ->
                    state.copy(
                        teeTimes = state.teeTimes.map { if (it.id == teeTimeId) updated else it },
                        isLoading = false,
                    )
                }
                onSuccess(result.manualInvites)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun removeInvite(teeTimeId: String, userId: String?, phone: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val updated = repository.removeTeeTimeInvite(teeTimeId, userId, phone)
                _uiState.update { state ->
                    state.copy(
                        teeTimes = state.teeTimes.map { if (it.id == teeTimeId) updated else it },
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    /** Bump a Linkside invitee for their RSVP. Non-Linkside invitees are bumped via SMS in the UI. */
    fun bumpInvitee(
        teeTimeId: String,
        userId: String?,
        phone: String?,
        onResult: (Boolean) -> Unit = {},
    ) {
        viewModelScope.launch {
            try {
                repository.bumpInvitee(teeTimeId, userId, phone)
                onResult(true)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
                onResult(false)
            }
        }
    }

    /** Toggle whether a Linkside invitee may invite others to this tee time (creator-only). */
    fun toggleInviteAccess(teeTimeId: String, userId: String) {
        viewModelScope.launch {
            try {
                val updated = repository.toggleInviteAccess(teeTimeId, userId)
                _uiState.update { state ->
                    state.copy(teeTimes = state.teeTimes.map { if (it.id == teeTimeId) updated else it })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /** Fetch the opt-in SMS message for a non-Linkside invitee, then hand it to the UI to open Messages. */
    fun prepareLinksideInvite(
        phone: String,
        name: String,
        hostName: String?,
        onReady: (com.linkside.app.data.model.ManualInvite) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val invite = repository.getOptInMessage(phone = phone, name = name, hostName = hostName)
                onReady(invite)
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
                    val exists = state.teeTimes.any { it.id == id }
                    state.copy(
                        teeTimes = if (exists) {
                            state.teeTimes.map { if (it.id == id) updated else it }
                        } else {
                            // Past / off-list tee times (e.g. round_recap) must be inserted.
                            listOf(updated) + state.teeTimes
                        },
                    )
                }
                onLoaded(updated)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun loadTeeTimeDetail(id: String) {
        viewModelScope.launch {
            try {
                val photos = repository.fetchTeeTimePhotos(id)
                val scores = repository.fetchRoundScores()
                val scorecards = runCatching { repository.fetchTeeTimeScorecards(id) }.getOrDefault(emptyList())
                _uiState.update { state ->
                    state.copy(
                        teeTimePhotos = state.teeTimePhotos + (id to photos),
                        roundScores = scores,
                        teeTimeScorecards = state.teeTimeScorecards + (id to scorecards),
                    )
                }
                val placeId = _uiState.value.teeTimes.firstOrNull { it.id == id }?.courseId
                if (!placeId.isNullOrBlank()) loadCourseWebsite(placeId)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /** Load AI round summary + scorecards for the post-round share screen. */
    fun loadRoundSummary(teeTimeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRoundSummary = true, errorMessage = null) }
            try {
                val summary = repository.fetchRoundSummary(teeTimeId)
                val scorecards = runCatching { repository.fetchTeeTimeScorecards(teeTimeId) }.getOrDefault(emptyList())
                _uiState.update { state ->
                    state.copy(
                        roundSummaries = state.roundSummaries + (teeTimeId to summary),
                        teeTimeScorecards = state.teeTimeScorecards + (teeTimeId to scorecards),
                        isLoadingRoundSummary = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingRoundSummary = false, errorMessage = e.message) }
            }
        }
    }

    /** Fetch and cache the course website for a Google Places placeId (best-effort). */
    fun loadCourseWebsite(placeId: String) {
        if (placeId.isBlank()) return
        if (_uiState.value.courseWebsites.containsKey(placeId)) return
        viewModelScope.launch {
            val website = repository.courseWebsite(placeId) ?: return@launch
            _uiState.update { state ->
                state.copy(courseWebsites = state.courseWebsites + (placeId to website))
            }
        }
    }

    fun loadMessages(teeTimeId: String) {
        viewModelScope.launch {
            try {
                val messages = repository.fetchTeeTimeMessages(teeTimeId)
                _uiState.update { state ->
                    state.copy(teeTimeMessages = state.teeTimeMessages + (teeTimeId to messages))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun startChatPolling(teeTimeId: String) {
        chatPollJob?.cancel()
        chatPollJob = viewModelScope.launch {
            while (isActive) {
                loadMessages(teeTimeId)
                delay(5_000)
            }
        }
    }

    fun stopChatPolling() {
        chatPollJob?.cancel()
        chatPollJob = null
    }

    fun sendMessage(teeTimeId: String, text: String, mentions: List<String> = emptyList()) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingMessage = true, errorMessage = null) }
            try {
                val message = repository.sendTeeTimeMessage(teeTimeId, trimmed, mentions)
                _uiState.update { state ->
                    val existing = state.teeTimeMessages[teeTimeId].orEmpty()
                    state.copy(
                        teeTimeMessages = state.teeTimeMessages + (teeTimeId to (existing + message)),
                        isSendingMessage = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSendingMessage = false, errorMessage = e.message) }
            }
        }
    }

    /** Toggle the current user's emoji reaction on a tee-time chat message. */
    fun toggleReaction(teeTimeId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            try {
                val updated = repository.toggleTeeTimeReaction(teeTimeId, messageId, emoji)
                _uiState.update { state ->
                    val existing = state.teeTimeMessages[teeTimeId].orEmpty()
                    state.copy(
                        teeTimeMessages = state.teeTimeMessages +
                            (teeTimeId to existing.map { if (it.id == updated.id) updated else it }),
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun uploadPhoto(teeTimeId: String, imageBytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, errorMessage = null) }
            try {
                val photo = repository.uploadTeeTimePhoto(teeTimeId, imageBytes, mimeType)
                _uiState.update { state ->
                    val existing = state.teeTimePhotos[teeTimeId].orEmpty()
                    state.copy(
                        teeTimePhotos = state.teeTimePhotos + (teeTimeId to (existing + photo)),
                        isUploadingPhoto = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploadingPhoto = false, errorMessage = e.message) }
            }
        }
    }

    fun saveRoundScore(teeTimeId: String, score: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingScore = true, errorMessage = null) }
            try {
                repository.saveRoundScore(teeTimeId, score)
                _uiState.update { state ->
                    state.copy(
                        roundScores = state.roundScores + (teeTimeId to score),
                        isSavingScore = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingScore = false, errorMessage = e.message) }
            }
        }
    }

    fun searchCourses(query: String, lat: Double? = null, lng: Double? = null) {
        if (query.length < 2) {
            clearCourseSearch()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingCourses = true) }
            try {
                val results = repository.searchCourses(query, lat, lng)
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

    /** Tee times that started within the last 5 hours (iOS "Current Round"). */
    fun currentRoundTeeTimes(user: User?): List<TeeTime> {
        val now = Instant.now()
        return _uiState.value.teeTimes.filter { teeTime ->
            val date = teeTime.parsedInstant() ?: return@filter false
            !date.isAfter(now) &&
                date.isAfter(now.minusSeconds(5 * 3600)) &&
                !teeTime.isDeclinedBy(user)
        }.sortedByDescending { it.parsedInstant() }
    }

    /**
     * Past rounds for Profile (iOS "Previous Tee Times"): started 5+ hours ago,
     * current user RSVP'd yes. Newest first.
     */
    fun previousTeeTimes(user: User?): List<TeeTime> {
        if (user == null) return emptyList()
        val cutoff = Instant.now().minusSeconds(5 * 3600)
        return _uiState.value.teeTimes.filter { teeTime ->
            val date = teeTime.parsedInstant() ?: return@filter false
            !date.isAfter(cutoff) &&
                teeTime.myInvite(user)?.inviteStatus == InviteStatus.YES
        }.sortedByDescending { it.parsedInstant() }
    }

    fun loadScorecardsForCurrentRounds(user: User?) {
        viewModelScope.launch {
            currentRoundTeeTimes(user).forEach { teeTime ->
                runCatching {
                    val cards = repository.fetchTeeTimeScorecards(teeTime.id)
                    _uiState.update { state ->
                        state.copy(teeTimeScorecards = state.teeTimeScorecards + (teeTime.id to cards))
                    }
                }
            }
        }
    }

    override fun onCleared() {
        stopChatPolling()
        super.onCleared()
    }

    class Factory(
        private val repository: LinksideRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TeeTimeViewModel(repository) as T
    }
}
