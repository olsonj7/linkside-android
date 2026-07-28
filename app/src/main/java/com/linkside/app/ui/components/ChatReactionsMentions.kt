package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkside.app.ui.theme.LinksideColors

/**
 * A person who can be @mentioned in a chat. [userId] is null for SMS-only
 * invitees — they can be tagged visually but won't receive a push.
 */
data class MentionCandidate(
    val userId: String?,
    val name: String,
) {
    val initial: String get() = name.trim().take(1).uppercase()
}

/**
 * Emoji reactions + @mention helpers shared by tee-time and golf-trip chats.
 * Mirrors iOS `MessageReactions` / `MentionSupport`.
 */
object ChatSocial {
    /** Quick-reaction emoji set. Must match backend `ALLOWED_REACTIONS`. */
    val quickReactions = listOf("\uD83D\uDC4D", "\u2764\uFE0F", "\uD83D\uDE02", "\uD83D\uDD25", "\u26F3\uFE0F", "\uD83C\uDF89")

    /** Ordered, non-empty reaction chips from a raw `emoji -> [userId]` map. */
    fun chips(reactions: Map<String, List<String>>?): List<Pair<String, List<String>>> {
        if (reactions.isNullOrEmpty()) return emptyList()
        return quickReactions.mapNotNull { emoji ->
            val ids = reactions[emoji]
            if (ids.isNullOrEmpty()) null else emoji to ids
        }
    }

    /**
     * The active @-query at the end of [text], or null if the user isn't currently
     * typing a mention. A mention-in-progress is a single whitespace-free token
     * right after an "@" that starts the text or follows whitespace.
     */
    fun activeMentionQuery(text: String): String? {
        val atIndex = text.lastIndexOf('@')
        if (atIndex < 0) return null
        if (atIndex > 0 && !text[atIndex - 1].isWhitespace()) return null
        val after = text.substring(atIndex + 1)
        if (after.any { it.isWhitespace() }) return null
        return after
    }

    /** Candidates matching the active query (prefix match on full name or any word). */
    fun filterMentions(
        candidates: List<MentionCandidate>,
        query: String,
        limit: Int = 6,
    ): List<MentionCandidate> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return candidates.take(limit)
        return candidates.filter { c ->
            val name = c.name.lowercase()
            name.startsWith(q) || name.split(" ").any { it.startsWith(q) }
        }.take(limit)
    }

    /** Replace the trailing @query with the chosen mention (adds a trailing space). */
    fun insertMention(candidate: MentionCandidate, text: String): String {
        val atIndex = text.lastIndexOf('@')
        if (atIndex < 0) return text
        return text.substring(0, atIndex) + "@${candidate.name} "
    }

    /** User IDs of candidates whose "@name" token appears in [text]. */
    fun mentionedUserIds(text: String, candidates: List<MentionCandidate>): List<String> {
        val lower = text.lowercase()
        val ids = LinkedHashSet<String>()
        for (c in candidates) {
            val uid = c.userId ?: continue
            if (lower.contains("@${c.name.lowercase()}")) ids.add(uid)
        }
        return ids.toList()
    }
}

/**
 * Build an [AnnotatedString] that highlights "@name" tokens of known candidates.
 * Longest names first so "@John Smith" wins over "@John".
 */
@Composable
fun highlightedMentions(
    text: String,
    candidates: List<MentionCandidate>,
    color: Color,
): AnnotatedString {
    if (candidates.isEmpty() || !text.contains('@')) return AnnotatedString(text)
    val ranges = mutableListOf<IntRange>()
    val names = candidates.map { it.name }.sortedByDescending { it.length }
    val lower = text.lowercase()
    for (name in names) {
        val token = "@${name.lowercase()}"
        if (token.length <= 1) continue
        var start = lower.indexOf(token)
        while (start >= 0) {
            val end = start + token.length
            if (ranges.none { start < it.last && end > it.first }) {
                ranges.add(start until end)
            }
            start = lower.indexOf(token, end)
        }
    }
    if (ranges.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        for (r in ranges) {
            addStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold), r.first, r.last + 1)
        }
    }
}

/** Horizontal row of quick-reaction emojis, shown when reacting to a message. */
@Composable
fun ReactionPickerRow(
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(LinksideColors.Card)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChatSocial.quickReactions.forEach { emoji ->
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onPick(emoji) }
                    .padding(6.dp),
            )
        }
    }
}

/** Compact row of reaction chips (emoji + count) shown under a message bubble. */
@Composable
fun ReactionChips(
    reactions: Map<String, List<String>>?,
    myId: String,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chips = ChatSocial.chips(reactions)
    if (chips.isEmpty()) return
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        chips.forEach { (emoji, ids) ->
            val mine = ids.contains(myId)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (mine) LinksideColors.AccentChipBackground else LinksideColors.Muted)
                    .then(
                        if (mine) Modifier.border(1.dp, LinksideColors.Accent, RoundedCornerShape(50))
                        else Modifier,
                    )
                    .clickable { onToggle(emoji) }
                    .padding(horizontal = 7.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = emoji, fontSize = 12.sp)
                Text(
                    text = "${ids.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (mine) LinksideColors.AccentLabel else LinksideColors.TextSecondary,
                )
            }
        }
    }
}

/** Tap-to-insert list shown above the chat input while typing an @mention. */
@Composable
fun MentionSuggestionList(
    candidates: List<MentionCandidate>,
    onSelect: (MentionCandidate) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (candidates.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LinksideColors.Card)
            .border(1.dp, LinksideColors.Muted, RoundedCornerShape(12.dp)),
    ) {
        candidates.forEachIndexed { index, candidate ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(candidate) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileAvatarView(name = candidate.name, size = 28.dp)
                Text(
                    text = candidate.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LinksideColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "@",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextTertiary,
                )
            }
            if (index != candidates.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = LinksideColors.Muted,
                )
            }
        }
    }
}
