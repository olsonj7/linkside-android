package com.linkside.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linkside.app.data.model.IdeaMessage
import com.linkside.app.data.model.IdeaThread
import com.linkside.app.data.repository.LinksideRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class IdeaThreadUiState(
    val threads: List<IdeaThread> = emptyList(),
    val threadMessages: Map<String, List<IdeaMessage>> = emptyMap(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isSendingMessage: Boolean = false,
    val isCreatingPoll: Boolean = false,
    val errorMessage: String? = null,
)

class IdeaThreadViewModel(
    private val repository: LinksideRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(IdeaThreadUiState())
    val uiState: StateFlow<IdeaThreadUiState> = _uiState.asStateFlow()

    private var chatPollJob: Job? = null

    fun loadThreads() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val threads = repository.fetchIdeaThreads()
                _uiState.update { it.copy(threads = threads, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun createThread(name: String, inviteePhones: List<String>, onSuccess: (IdeaThread) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            try {
                val thread = repository.createIdeaThread(name, inviteePhones)
                _uiState.update { state ->
                    state.copy(threads = listOf(thread) + state.threads, isCreating = false)
                }
                onSuccess(thread)
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreating = false, errorMessage = e.message) }
            }
        }
    }

    fun loadMessages(threadId: String) {
        viewModelScope.launch {
            try {
                val messages = repository.fetchIdeaMessages(threadId)
                _uiState.update { state ->
                    state.copy(threadMessages = state.threadMessages + (threadId to messages))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun startChatPolling(threadId: String) {
        chatPollJob?.cancel()
        chatPollJob = viewModelScope.launch {
            while (isActive) {
                loadMessages(threadId)
                delay(5_000)
            }
        }
    }

    fun stopChatPolling() {
        chatPollJob?.cancel()
        chatPollJob = null
    }

    fun sendMessage(threadId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingMessage = true, errorMessage = null) }
            try {
                val message = repository.sendIdeaMessage(threadId, trimmed)
                _uiState.update { state ->
                    val existing = state.threadMessages[threadId].orEmpty()
                    state.copy(
                        threadMessages = state.threadMessages + (threadId to (existing + message)),
                        isSendingMessage = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSendingMessage = false, errorMessage = e.message) }
            }
        }
    }

    fun createPoll(
        threadId: String,
        question: String,
        options: List<String>,
        allowMultiple: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingPoll = true, errorMessage = null) }
            try {
                val message = repository.createIdeaThreadPoll(threadId, question, options, allowMultiple)
                _uiState.update { state ->
                    val existing = state.threadMessages[threadId].orEmpty()
                    state.copy(
                        threadMessages = state.threadMessages + (threadId to (existing + message)),
                        isCreatingPoll = false,
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreatingPoll = false, errorMessage = e.message) }
            }
        }
    }

    fun votePoll(threadId: String, pollId: String, optionIds: List<String>) {
        viewModelScope.launch {
            try {
                val poll = repository.votePoll(pollId, optionIds)
                updatePoll(threadId, poll)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun closePoll(threadId: String, pollId: String) {
        viewModelScope.launch {
            try {
                val poll = repository.closePoll(pollId)
                updatePoll(threadId, poll)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun deletePoll(threadId: String, pollId: String) {
        viewModelScope.launch {
            try {
                repository.deletePoll(pollId)
                _uiState.update { state ->
                    val existing = state.threadMessages[threadId].orEmpty()
                    state.copy(
                        threadMessages = state.threadMessages +
                            (threadId to existing.filterNot { it.poll?.id == pollId }),
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun updatePoll(threadId: String, poll: com.linkside.app.data.model.Poll) {
        _uiState.update { state ->
            val existing = state.threadMessages[threadId].orEmpty()
            state.copy(
                threadMessages = state.threadMessages + (threadId to existing.map { msg ->
                    if (msg.poll?.id == poll.id) msg.copy(poll = poll) else msg
                }),
            )
        }
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            try {
                repository.deleteIdeaThread(threadId)
                _uiState.update { state ->
                    state.copy(threads = state.threads.filter { it.id != threadId })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
            IdeaThreadViewModel(repository) as T
    }
}
