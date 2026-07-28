package com.linkside.app.data.model

import com.squareup.moshi.Json

/**
 * A poll attached to an idea thread or golf-trip chat. Options carry live vote
 * tallies; [myVotes] lists the option ids the current user has selected.
 * Mirrors iOS `Poll`.
 */
data class Poll(
    val id: String,
    @Json(name = "creator_id") val creatorId: String,
    val question: String,
    val options: List<PollOption> = emptyList(),
    @Json(name = "allow_multiple") val allowMultiple: Boolean = false,
    val closed: Boolean = false,
    @Json(name = "my_votes") val myVotes: List<String> = emptyList(),
    @Json(name = "voter_count") val voterCount: Int = 0,
) {
    val totalVotes: Int get() = options.sumOf { it.votes }
    val hasVoted: Boolean get() = myVotes.isNotEmpty()

    fun isSelected(optionId: String): Boolean = myVotes.contains(optionId)

    fun canManage(userId: String, contextCreatorId: String?): Boolean =
        creatorId == userId || contextCreatorId == userId
}

data class PollOption(
    val id: String,
    val text: String,
    val votes: Int = 0,
)

/** Body for creating a poll in an idea thread or golf-trip chat. */
data class CreatePollRequest(
    val question: String,
    val options: List<String>,
    val allowMultiple: Boolean,
)

/** Body for casting/replacing the current user's vote(s) on a poll. */
data class VotePollRequest(
    val optionIds: List<String>,
)

/** Response for vote/close endpoints — returns the updated poll. */
data class PollResponse(
    val ok: Boolean,
    val poll: Poll? = null,
    val error: String? = null,
)
