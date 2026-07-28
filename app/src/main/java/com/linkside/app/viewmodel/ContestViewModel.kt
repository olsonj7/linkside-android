package com.linkside.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linkside.app.data.model.ContestLeaderboard
import com.linkside.app.data.model.ContestWin
import com.linkside.app.data.model.ReferralSummary
import com.linkside.app.data.repository.LinksideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContestUiState(
    val summary: ReferralSummary? = null,
    val contest: ContestLeaderboard? = null,
    val win: ContestWin? = null,
    val isLoading: Boolean = false,
    val isClaiming: Boolean = false,
    val errorMessage: String? = null,
    val claimError: String? = null,
)

class ContestViewModel(
    private val repository: LinksideRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContestUiState())
    val uiState: StateFlow<ContestUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val summary = runCatching { repository.fetchReferralSummary() }.getOrNull()
                val contest = repository.fetchContestLeaderboard()
                val win = runCatching { repository.fetchMyContestClaim() }.getOrNull()
                _uiState.update {
                    it.copy(
                        summary = summary,
                        contest = contest,
                        win = win,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Couldn't load contest")
                }
            }
        }
    }

    fun claimPrize(
        month: String,
        name: String,
        email: String?,
        address: String,
        city: String?,
        state: String?,
        zip: String?,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isClaiming = true, claimError = null) }
            try {
                repository.claimContestPrize(month, name, email, address, city, state, zip)
                val win = runCatching { repository.fetchMyContestClaim() }.getOrNull()
                    ?: _uiState.value.win?.copy(claimed = true)
                _uiState.update { it.copy(isClaiming = false, win = win) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isClaiming = false,
                        claimError = e.message ?: "Couldn't submit your claim. Please try again.",
                    )
                }
            }
        }
    }

    fun clearErrors() {
        _uiState.update { it.copy(errorMessage = null, claimError = null) }
    }

    class Factory(
        private val repository: LinksideRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ContestViewModel(repository) as T
    }
}
