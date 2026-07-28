package com.linkside.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linkside.app.data.model.Tournament
import com.linkside.app.data.model.TournamentParticipant
import com.linkside.app.data.model.TournamentProduct
import com.linkside.app.data.repository.LinksideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TournamentUiState(
    val tournaments: List<Tournament> = emptyList(),
    val selected: Tournament? = null,
    val participants: List<TournamentParticipant> = emptyList(),
    val products: List<TournamentProduct> = emptyList(),
    val isLoading: Boolean = false,
    val isRegistering: Boolean = false,
    val isWithdrawing: Boolean = false,
    val errorMessage: String? = null,
)

class TournamentViewModel(
    private val repository: LinksideRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TournamentUiState())
    val uiState: StateFlow<TournamentUiState> = _uiState.asStateFlow()

    fun loadTournaments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val list = repository.fetchTournaments()
                    .filter { it.status == "open" || it.isRegistered || it.isWithdrawn }
                    .sortedBy { it.date ?: Long.MAX_VALUE }
                _uiState.update { it.copy(tournaments = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun loadTournament(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val (tournament, participants) = repository.fetchTournament(id)
                val products = runCatching { repository.fetchTournamentProducts(id) }.getOrDefault(emptyList())
                _uiState.update {
                    it.copy(
                        selected = tournament,
                        participants = participants,
                        products = products,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun register(id: String, teamName: String? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRegistering = true, errorMessage = null) }
            try {
                val products = _uiState.value.products
                val entry = products.firstOrNull { it.isAddon != true }
                val productIds = listOfNotNull(entry?.id)
                repository.registerForTournament(id, productIds = productIds, teamName = teamName)
                loadTournament(id)
                loadTournaments()
                _uiState.update { it.copy(isRegistering = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isRegistering = false, errorMessage = e.message) }
            }
        }
    }

    fun withdraw(id: String, ref: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWithdrawing = true, errorMessage = null) }
            try {
                repository.withdrawFromTournament(id, ref)
                loadTournament(id)
                loadTournaments()
                _uiState.update { it.copy(isWithdrawing = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isWithdrawing = false, errorMessage = e.message) }
            }
        }
    }

    /** Upcoming tournaments the current user withdrew from (can re-register). */
    fun withdrawnTournaments(): List<Tournament> {
        val todayStart = java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return _uiState.value.tournaments
            .filter { it.isWithdrawn && (it.date == null || it.date >= todayStart) }
            .sortedBy { it.date ?: Long.MAX_VALUE }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val repository: LinksideRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TournamentViewModel(repository) as T
    }
}
