package com.linkside.app.ui.teetimes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.TeeTimeChatMessage
import com.linkside.app.data.model.User
import com.linkside.app.ui.components.ChatBubble
import com.linkside.app.ui.components.ChatSocial
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.MentionCandidate
import com.linkside.app.ui.components.MentionSuggestionList
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeeTimeChatScreen(
    courseName: String,
    teeTimeId: String,
    user: User,
    messages: List<TeeTimeChatMessage>,
    isSending: Boolean,
    mentionCandidates: List<MentionCandidate>,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onStartPolling: () -> Unit,
    onStopPolling: () -> Unit,
    onSend: (String, List<String>) -> Unit,
    onToggleReaction: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val mentionMatches = ChatSocial.activeMentionQuery(draft)?.let {
        ChatSocial.filterMentions(mentionCandidates, it)
    }.orEmpty()

    LaunchedEffect(teeTimeId) {
        onLoad()
        onStartPolling()
    }

    DisposableEffect(teeTimeId) {
        onDispose { onStopPolling() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = { LinksideTopAppBar(title = courseName, onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No messages yet. Say hello to the group!", color = LinksideColors.TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(
                            senderName = message.senderName,
                            text = message.text,
                            timeLabel = message.formattedTime(),
                            isMine = message.senderId == user.id,
                            reactions = message.reactions,
                            myId = user.id,
                            mentionCandidates = mentionCandidates,
                            onToggleReaction = { emoji -> onToggleReaction(message.id, emoji) },
                        )
                    }
                }
            }

            if (mentionMatches.isNotEmpty()) {
                MentionSuggestionList(
                    candidates = mentionMatches,
                    onSelect = { candidate -> draft = ChatSocial.insertMention(candidate, draft) },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message…") },
                    maxLines = 4,
                    enabled = !isSending,
                )
                IconButton(
                    onClick = {
                        val text = draft
                        val mentions = ChatSocial.mentionedUserIds(text, mentionCandidates)
                        draft = ""
                        onSend(text, mentions)
                    },
                    enabled = draft.isNotBlank() && !isSending,
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp),
                            color = LinksideColors.Accent,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}
