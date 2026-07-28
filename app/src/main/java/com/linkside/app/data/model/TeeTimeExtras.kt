package com.linkside.app.data.model

import com.squareup.moshi.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TeeTimeChatMessage(
    val id: String,
    @Json(name = "tee_time_id") val teeTimeId: String? = null,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "sender_name") val senderName: String,
    val text: String,
    /** Emoji → list of user IDs who reacted. */
    val reactions: Map<String, List<String>> = emptyMap(),
    @Json(name = "created_at") val createdAt: Double,
) {
    fun formattedTime(): String {
        val instant = Instant.ofEpochMilli(createdAt.toLong())
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        return formatter.format(instant.atZone(ZoneId.systemDefault()))
    }
}

data class TeeTimeMessagesResponse(
    val ok: Boolean,
    val messages: List<TeeTimeChatMessage> = emptyList(),
)

data class TeeTimeMessageResponse(
    val ok: Boolean,
    val message: TeeTimeChatMessage? = null,
    val error: String? = null,
)

data class SendTeeTimeMessageRequest(
    val text: String,
    val mentions: List<String> = emptyList(),
)

/** Body for toggling an emoji reaction on a chat message (tee time or trip). */
data class MessageReactionRequest(
    val emoji: String,
)

data class SaveRoundScoreRequest(
    val score: Int,
)

data class RoundScoreEntry(
    val teeTimeId: String,
    val score: Int,
)

data class RoundScoresResponse(
    val ok: Boolean,
    val scores: List<RoundScoreEntry> = emptyList(),
)

data class IdeaInvitee(
    @Json(name = "user_id") val userId: String,
    val name: String,
)

data class IdeaThread(
    val id: String,
    val name: String,
    @Json(name = "creator_id") val creatorId: String,
    val invitees: List<IdeaInvitee> = emptyList(),
    val status: String,
    @Json(name = "converted_tee_time_id") val convertedTeeTimeId: String? = null,
    @Json(name = "created_at") val createdAt: Double,
    @Json(name = "updated_at") val updatedAt: Double,
) {
    val isBrainstorming: Boolean get() = status == "brainstorming"
}

data class IdeaThreadsResponse(
    val ok: Boolean,
    val threads: List<IdeaThread> = emptyList(),
)

data class IdeaThreadResponse(
    val ok: Boolean,
    val thread: IdeaThread? = null,
    val error: String? = null,
)

data class CreateIdeaThreadRequest(
    val name: String,
    val inviteePhones: List<String>,
)

data class IdeaMessage(
    val id: String,
    @Json(name = "thread_id") val threadId: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "sender_name") val senderName: String,
    val text: String,
    /** "text" or "poll". */
    val kind: String? = null,
    val poll: Poll? = null,
    @Json(name = "created_at") val createdAt: Double,
) {
    val isPoll: Boolean get() = kind == "poll" && poll != null

    fun formattedTime(): String {
        val instant = Instant.ofEpochMilli(createdAt.toLong())
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        return formatter.format(instant.atZone(ZoneId.systemDefault()))
    }
}

data class IdeaMessagesResponse(
    val ok: Boolean,
    val messages: List<IdeaMessage> = emptyList(),
)

data class IdeaMessageResponse(
    val ok: Boolean,
    val message: IdeaMessage? = null,
    val error: String? = null,
)

data class SendIdeaMessageRequest(
    val text: String,
)

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val refId: String? = null,
    val read: Boolean,
    val createdAt: Double,
) {
    fun isTeeTimeRelated(): Boolean =
        type.startsWith("tee_time_") || type == "round_recap"

    fun isTripRelated(): Boolean =
        type.startsWith("trip_")

    fun isIdeaThreadRelated(): Boolean =
        type.startsWith("idea_thread_")

    fun isTournamentRelated(): Boolean =
        type.startsWith("tournament_") || type == "team_invite"

    fun isRoundRecap(): Boolean = type == "round_recap"

    fun isContestWinner(): Boolean = type == "contest_winner"

    /** Chat message / poll notifications that should open the relevant chat thread. */
    fun opensChat(): Boolean =
        type == "tee_time_message" ||
            type == "trip_message" ||
            type == "trip_poll" ||
            type == "idea_thread_message" ||
            type == "idea_thread_poll"
}

data class NotificationsResponse(
    val ok: Boolean,
    val notifications: List<AppNotification> = emptyList(),
)

data class MarkNotificationsReadRequest(
    val ids: List<String>? = null,
)

data class DeviceTokenRequest(
    val token: String,
    val platform: String = "android",
)

data class LinkPhoneRequest(
    val phone: String,
    val code: String,
)
