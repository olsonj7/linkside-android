package com.linkside.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linkside.app.data.model.GolfTrip
import com.linkside.app.data.model.InviteStatus
import com.linkside.app.data.model.Photo
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TripChatMessage
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

data class TripUiState(
    val trips: List<GolfTrip> = emptyList(),
    val tripTeeTimes: Map<String, List<TeeTime>> = emptyMap(),
    val tripPhotos: Map<String, List<Photo>> = emptyMap(),
    val tripMessages: Map<String, List<TripChatMessage>> = emptyMap(),
    val tripAnnouncements: Map<String, List<com.linkside.app.data.model.TripAnnouncement>> = emptyMap(),
    val isLoading: Boolean = false,
    val isSendingMessage: Boolean = false,
    val isCreatingPoll: Boolean = false,
    val isPostingAnnouncement: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val errorMessage: String? = null,
)

class TripViewModel(
    private val repository: LinksideRepository,
) : ViewModel() {
    var currentUser: User? = null

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    private var chatPollJob: Job? = null

    fun loadTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val all = repository.fetchGolfTrips()
                val user = currentUser
                val filtered = all.filter { trip ->
                    !trip.isDeclinedBy(user) || trip.isActiveDeclined(user)
                }
                _uiState.update { it.copy(trips = filtered, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun refreshTrip(id: String) {
        viewModelScope.launch {
            try {
                val trip = repository.fetchGolfTrip(id)
                upsertTrip(trip)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun loadTripDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val trip = repository.fetchGolfTrip(id)
                val teeTimes = repository.fetchTripTeeTimes(id)
                val photos = repository.fetchTripPhotos(id)
                val announcements = repository.fetchTripAnnouncements(id)
                upsertTrip(trip)
                _uiState.update { state ->
                    state.copy(
                        tripTeeTimes = state.tripTeeTimes + (id to teeTimes),
                        tripPhotos = state.tripPhotos + (id to photos),
                        tripAnnouncements = state.tripAnnouncements + (id to announcements),
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun postAnnouncement(tripId: String, message: String, onSuccess: () -> Unit = {}) {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPostingAnnouncement = true, errorMessage = null) }
            try {
                val announcement = repository.postTripAnnouncement(tripId, trimmed)
                _uiState.update { state ->
                    val existing = state.tripAnnouncements[tripId].orEmpty()
                    state.copy(
                        tripAnnouncements = state.tripAnnouncements + (tripId to (listOf(announcement) + existing)),
                        isPostingAnnouncement = false,
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isPostingAnnouncement = false, errorMessage = e.message) }
            }
        }
    }

    fun rsvpTrip(tripId: String, status: InviteStatus) {
        val user = currentUser
        // Optimistic update so Home hides and Profile declined list updates immediately.
        _uiState.update { state ->
            state.copy(
                trips = state.trips.map { trip ->
                    if (trip.id != tripId) trip
                    else trip.withInviteStatusFor(user, status)
                },
            )
        }
        viewModelScope.launch {
            try {
                val updated = repository.rsvpGolfTrip(tripId, status.raw)
                upsertTrip(updated)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
                loadTrips()
            }
        }
    }

    fun setDepositPaid(tripId: String, phone: String?, userId: String?, paid: Boolean) {
        viewModelScope.launch {
            try {
                val updated = repository.setTripDepositPaid(tripId, phone, userId, paid)
                upsertTrip(updated)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun setBalancePaid(tripId: String, phone: String?, userId: String?, paid: Boolean) {
        viewModelScope.launch {
            try {
                val updated = repository.setTripBalancePaid(tripId, phone, userId, paid)
                upsertTrip(updated)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun loadMessages(tripId: String) {
        viewModelScope.launch {
            try {
                val messages = repository.fetchTripMessages(tripId)
                _uiState.update { state ->
                    state.copy(tripMessages = state.tripMessages + (tripId to messages))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun startChatPolling(tripId: String) {
        chatPollJob?.cancel()
        chatPollJob = viewModelScope.launch {
            while (isActive) {
                loadMessages(tripId)
                delay(5_000)
            }
        }
    }

    fun stopChatPolling() {
        chatPollJob?.cancel()
        chatPollJob = null
    }

    fun sendMessage(tripId: String, text: String, mentions: List<String> = emptyList(), onSent: () -> Unit = {}) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingMessage = true, errorMessage = null) }
            try {
                val message = repository.sendTripMessage(tripId, trimmed, mentions)
                _uiState.update { state ->
                    val existing = state.tripMessages[tripId].orEmpty()
                    state.copy(
                        tripMessages = state.tripMessages + (tripId to (existing + message)),
                        isSendingMessage = false,
                    )
                }
                onSent()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSendingMessage = false, errorMessage = e.message) }
            }
        }
    }

    /** Toggle the current user's emoji reaction on a trip chat message. */
    fun toggleReaction(tripId: String, messageId: String, emoji: String) {
        viewModelScope.launch {
            try {
                val updated = repository.toggleTripReaction(tripId, messageId, emoji)
                _uiState.update { state ->
                    val existing = state.tripMessages[tripId].orEmpty()
                    state.copy(
                        tripMessages = state.tripMessages +
                            (tripId to existing.map { if (it.id == updated.id) updated else it }),
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun createPoll(
        tripId: String,
        question: String,
        options: List<String>,
        allowMultiple: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingPoll = true, errorMessage = null) }
            try {
                val message = repository.createTripPoll(tripId, question, options, allowMultiple)
                _uiState.update { state ->
                    val existing = state.tripMessages[tripId].orEmpty()
                    state.copy(
                        tripMessages = state.tripMessages + (tripId to (existing + message)),
                        isCreatingPoll = false,
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreatingPoll = false, errorMessage = e.message) }
            }
        }
    }

    fun votePoll(tripId: String, pollId: String, optionIds: List<String>) {
        viewModelScope.launch {
            try {
                val poll = repository.votePoll(pollId, optionIds)
                updatePoll(tripId, poll)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun closePoll(tripId: String, pollId: String) {
        viewModelScope.launch {
            try {
                val poll = repository.closePoll(pollId)
                updatePoll(tripId, poll)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun deletePoll(tripId: String, pollId: String) {
        viewModelScope.launch {
            try {
                repository.deletePoll(pollId)
                _uiState.update { state ->
                    val existing = state.tripMessages[tripId].orEmpty()
                    state.copy(
                        tripMessages = state.tripMessages +
                            (tripId to existing.filterNot { it.poll?.id == pollId }),
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun updatePoll(tripId: String, poll: com.linkside.app.data.model.Poll) {
        _uiState.update { state ->
            val existing = state.tripMessages[tripId].orEmpty()
            state.copy(
                tripMessages = state.tripMessages + (tripId to existing.map { msg ->
                    if (msg.poll?.id == poll.id) msg.copy(poll = poll) else msg
                }),
            )
        }
    }

    fun uploadPhoto(tripId: String, imageBytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, errorMessage = null) }
            try {
                val photo = repository.uploadTripPhoto(tripId, imageBytes, mimeType)
                _uiState.update { state ->
                    val existing = state.tripPhotos[tripId].orEmpty()
                    state.copy(
                        tripPhotos = state.tripPhotos + (tripId to (existing + photo)),
                        isUploadingPhoto = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploadingPhoto = false, errorMessage = e.message) }
            }
        }
    }

    fun trip(id: String): GolfTrip? = _uiState.value.trips.firstOrNull { it.id == id }

    fun upcomingTrips(user: User?): List<GolfTrip> {
        val now = Instant.now()
        return _uiState.value.trips.filter { trip ->
            val end = trip.parsedEnd() ?: trip.parsedStart()
            end == null || end.isAfter(now.minusSeconds(24 * 3600))
        }.filter { !it.isDeclinedBy(user) }
    }

    fun declinedTrips(user: User?): List<GolfTrip> {
        return _uiState.value.trips
            .filter { it.isActiveDeclined(user) }
            .sortedByDescending { it.parsedStart() ?: Instant.EPOCH }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        stopChatPolling()
        super.onCleared()
    }

    private fun upsertTrip(trip: GolfTrip) {
        _uiState.update { state ->
            val exists = state.trips.any { it.id == trip.id }
            val trips = if (exists) {
                state.trips.map { if (it.id == trip.id) trip else it }
            } else {
                listOf(trip) + state.trips
            }
            state.copy(trips = trips.sortedBy { it.parsedStart() ?: Instant.EPOCH })
        }
    }

    class Factory(
        private val repository: LinksideRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TripViewModel(repository) as T
    }
}
