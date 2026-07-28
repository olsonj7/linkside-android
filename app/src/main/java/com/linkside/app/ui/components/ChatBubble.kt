package com.linkside.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    senderName: String,
    text: String,
    timeLabel: String,
    isMine: Boolean,
    modifier: Modifier = Modifier,
    reactions: Map<String, List<String>> = emptyMap(),
    myId: String = "",
    mentionCandidates: List<MentionCandidate> = emptyList(),
    onToggleReaction: ((String) -> Unit)? = null,
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        if (!isMine) {
            Text(
                text = senderName,
                style = MaterialTheme.typography.labelSmall,
                color = LinksideColors.TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            )
        }
        Box {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = if (isMine) LinksideColors.AccentLabelLight else LinksideColors.Card,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .then(
                        if (onToggleReaction != null) {
                            Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { showPicker = true },
                            )
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Column {
                    Text(
                        text = highlightedMentions(text, mentionCandidates, LinksideColors.AccentLabel),
                        color = LinksideColors.TextPrimary,
                    )
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = LinksideColors.TextTertiary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            if (showPicker && onToggleReaction != null) {
                Popup(
                    alignment = Alignment.TopCenter,
                    offset = IntOffset(0, -140),
                    onDismissRequest = { showPicker = false },
                ) {
                    ReactionPickerRow(
                        onPick = { emoji ->
                            showPicker = false
                            onToggleReaction(emoji)
                        },
                    )
                }
            }
        }

        if (onToggleReaction != null) {
            ReactionChips(
                reactions = reactions,
                myId = myId,
                onToggle = onToggleReaction,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
