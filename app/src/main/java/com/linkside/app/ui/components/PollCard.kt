package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.Poll
import com.linkside.app.data.model.PollOption
import com.linkside.app.ui.theme.LinksideColors
import kotlin.math.roundToInt

/**
 * Inline chat card for a poll in an idea thread or golf-trip chat. Shows live
 * tallies for every option; tapping an option casts/updates a vote. Mirrors iOS
 * `PollCardView`.
 */
@Composable
fun PollCard(
    poll: Poll,
    senderName: String,
    time: String,
    canManage: Boolean,
    onVote: (List<String>) -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PollHeader(poll = poll, canManage = canManage, onClose = onClose, onDelete = onDelete)
        poll.options.forEach { option ->
            PollOptionRow(poll = poll, option = option, onVote = onVote)
        }
        PollFooter(poll = poll, senderName = senderName, time = time)
    }
}

@Composable
private fun PollHeader(
    poll: Poll,
    canManage: Boolean,
    onClose: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = poll.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextPrimary,
            )
            Text(
                text = if (poll.allowMultiple) "Poll · select all that apply" else "Poll · pick one",
                style = MaterialTheme.typography.labelSmall,
                color = LinksideColors.TextTertiary,
            )
        }
        if (poll.closed) {
            Text(
                text = "CLOSED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = LinksideColors.TextSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(LinksideColors.Muted)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        if (canManage) {
            var menuOpen by remember { mutableStateOf(false) }
            Box {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Poll options",
                    tint = LinksideColors.TextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { menuOpen = true }
                        .padding(2.dp),
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (!poll.closed) {
                        DropdownMenuItem(
                            text = { Text("Close poll") },
                            onClick = {
                                menuOpen = false
                                onClose()
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete poll", color = LinksideColors.Danger) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PollOptionRow(
    poll: Poll,
    option: PollOption,
    onVote: (List<String>) -> Unit,
) {
    val selected = poll.isSelected(option.id)
    val total = poll.totalVotes
    val fraction = if (total == 0) 0f else option.votes.toFloat() / total.toFloat()
    val percent = (fraction * 100).roundToInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LinksideColors.Muted.copy(alpha = 0.25f))
            .then(
                if (selected) {
                    Modifier.border(1.5.dp, LinksideColors.Accent.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                } else {
                    Modifier
                },
            )
            .clickable(enabled = !poll.closed) { handleTap(poll, option.id, onVote) },
    ) {
        // Progress fill behind the row content. matchParentSize matches the Row's
        // measured height without contributing to sizing; the inner box then takes
        // a fraction of the width for the tally bar.
        if (fraction > 0f) {
            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(
                            if (selected) LinksideColors.Accent.copy(alpha = 0.28f)
                            else LinksideColors.Muted.copy(alpha = 0.6f),
                        ),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = optionIcon(selected, poll.allowMultiple),
                contentDescription = null,
                tint = if (selected) LinksideColors.AccentLabel else LinksideColors.TextTertiary,
            )
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyMedium,
                color = LinksideColors.TextPrimary,
                maxLines = 2,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextSecondary,
            )
            Text(
                text = "(${option.votes})",
                style = MaterialTheme.typography.labelSmall,
                color = LinksideColors.TextTertiary,
            )
        }
    }
}

@Composable
private fun PollFooter(poll: Poll, senderName: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val voteLabel = if (poll.voterCount == 1) "vote" else "votes"
        Text(
            text = "${poll.voterCount} $voteLabel · $senderName",
            style = MaterialTheme.typography.labelSmall,
            color = LinksideColors.TextTertiary,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = LinksideColors.TextTertiary,
        )
    }
}

private fun optionIcon(selected: Boolean, allowMultiple: Boolean) = when {
    selected && allowMultiple -> Icons.Default.CheckBox
    selected && !allowMultiple -> Icons.Default.RadioButtonChecked
    !selected && allowMultiple -> Icons.Default.CheckBoxOutlineBlank
    else -> Icons.Default.RadioButtonUnchecked
}

private fun handleTap(poll: Poll, optionId: String, onVote: (List<String>) -> Unit) {
    if (poll.closed) return
    if (poll.allowMultiple) {
        val set = poll.myVotes.toMutableSet()
        if (!set.add(optionId)) set.remove(optionId)
        // Multi-select requires at least one option; ignore taps that would clear all.
        if (set.isEmpty()) return
        onVote(set.toList())
    } else {
        if (poll.myVotes == listOf(optionId)) return
        onVote(listOf(optionId))
    }
}
