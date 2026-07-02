package com.linkside.app.ui.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.TripChatMessage
import com.linkside.app.data.model.User
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripChatScreen(
    tripName: String,
    tripId: String,
    user: User,
    messages: List<TripChatMessage>,
    isSending: Boolean,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onStartPolling: () -> Unit,
    onStopPolling: () -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(tripId) {
        onLoad()
        onStartPolling()
    }

    androidx.compose.runtime.DisposableEffect(tripId) {
        onDispose { onStopPolling() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(tripName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
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
                    Text("No messages yet. Start the trip chat below!", color = LinksideColors.TextSecondary)
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
                        MessageBubble(message = message, isMine = message.senderId == user.id)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LinksideColors.Muted)
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
                        draft = ""
                        onSend(text)
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

@Composable
private fun MessageBubble(message: TripChatMessage, isMine: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        if (!isMine) {
            Text(
                text = message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = LinksideColors.TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isMine) LinksideColors.AccentLabelLight else LinksideColors.Card,
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Column {
                Text(
                    text = message.text,
                    color = if (isMine) LinksideColors.TextPrimary else LinksideColors.TextPrimary,
                )
                Text(
                    text = message.formattedTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = LinksideColors.TextTertiary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
