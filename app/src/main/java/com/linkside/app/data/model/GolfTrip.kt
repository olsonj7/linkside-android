package com.linkside.app.data.model

import com.squareup.moshi.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class GolfTrip(
    val id: String,
    val name: String,
    val location: String,
    val startDate: Double,
    val endDate: Double,
    val costPerPerson: Double? = null,
    val golfersNeeded: Int,
    val creatorId: String,
    val invites: List<Invite> = emptyList(),
    val notes: String? = null,
    val venmoHandle: String? = null,
    val deposit: Double? = null,
    val createdAt: Double? = null,
    val resortPlaceId: String? = null,
    val resortLink: String? = null,
    val paymentLink: String? = null,
) {
    val yesCount: Int get() = invites.count { it.inviteStatus == InviteStatus.YES }
    val isFull: Boolean get() = yesCount >= golfersNeeded

    fun myInvite(user: User?): Invite? = invites.firstOrNull { it.matchesUser(user) }

    fun isHost(user: User?): Boolean = creatorId == user?.id

    fun isDeclinedBy(user: User?): Boolean = myInvite(user)?.inviteStatus == InviteStatus.NO

    /** Keep declined trips visible for 24 hours after end date (matches iOS). */
    fun isActiveDeclined(user: User?, now: Instant = Instant.now()): Boolean {
        if (!isDeclinedBy(user)) return false
        val end = parsedEnd() ?: parsedStart() ?: return false
        return end.isAfter(now.minusSeconds(24 * 3600))
    }

    /** Local RSVP patch for optimistic UI before the server round-trip completes. */
    fun withInviteStatusFor(
        user: User?,
        status: InviteStatus,
    ): GolfTrip {
        return copy(
            invites = invites.map { invite ->
                if (invite.matchesUser(user)) invite.copy(status = status.raw) else invite
            },
        )
    }

    fun parsedStart(): Instant? = TripDates.parse(startDate)

    fun parsedEnd(): Instant? = TripDates.parse(endDate)

    fun formattedDateRange(): String {
        val start = parsedStart() ?: return ""
        val end = parsedEnd() ?: return ""
        val zone = ZoneId.systemDefault()
        val startZ = start.atZone(zone)
        val endZ = end.atZone(zone)
        val monthDay = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
        if (startZ.toLocalDate() == endZ.toLocalDate()) {
            val full = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
            return full.format(startZ)
        }
        return "${monthDay.format(startZ)} – ${monthDay.format(endZ)}"
    }

    fun formattedCost(): String? {
        val cost = costPerPerson ?: return null
        if (cost <= 0) return null
        return "$${cost.toInt()}/person"
    }

    fun formattedDeposit(): String? {
        val amount = deposit ?: return null
        if (amount <= 0) return null
        return "$${amount.toInt()} deposit"
    }
}

object TripDates {
    fun parse(raw: Double?): Instant? {
        if (raw == null) return null
        return Instant.ofEpochMilli(raw.toLong())
    }
}

data class GolfTripsListResponse(
    val ok: Boolean,
    val trips: List<GolfTrip> = emptyList(),
)

data class GolfTripResponse(
    val ok: Boolean,
    val trip: GolfTrip? = null,
    val error: String? = null,
)

data class TripRsvpRequest(
    val status: String,
)

data class TripPaymentRequest(
    val paid: Boolean,
    val phone: String? = null,
    val userId: String? = null,
)

data class TripChatMessage(
    val id: String,
    @Json(name = "trip_id") val tripId: String? = null,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "sender_name") val senderName: String,
    val text: String,
    /** Emoji → list of user IDs who reacted. */
    val reactions: Map<String, List<String>> = emptyMap(),
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

data class TripMessagesResponse(
    val ok: Boolean,
    val messages: List<TripChatMessage> = emptyList(),
)

data class TripMessageResponse(
    val ok: Boolean,
    val message: TripChatMessage? = null,
    val error: String? = null,
)

data class SendTripMessageRequest(
    val text: String,
    val mentions: List<String> = emptyList(),
)

/** A host-posted announcement on a golf trip. Mirrors iOS `TripAnnouncement`. */
data class TripAnnouncement(
    val id: String,
    val tripId: String,
    val message: String,
    val createdAt: Double,
) {
    fun formattedDate(): String {
        val instant = Instant.ofEpochMilli(createdAt.toLong())
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a", Locale.getDefault())
        return formatter.format(instant.atZone(ZoneId.systemDefault()))
    }
}

data class TripAnnouncementsResponse(
    val ok: Boolean,
    val announcements: List<TripAnnouncement> = emptyList(),
)

data class PostAnnouncementRequest(
    val message: String,
)

data class PostAnnouncementResponse(
    val ok: Boolean,
    val announcement: TripAnnouncement? = null,
    val error: String? = null,
)

data class Photo(
    val id: String,
    val url: String,
    val uploaderId: String,
    val createdAt: Double,
    val tripId: String? = null,
    val teeTimeId: String? = null,
)

data class PhotosResponse(
    val ok: Boolean,
    val photos: List<Photo> = emptyList(),
)

data class PhotoResponse(
    val ok: Boolean,
    val photo: Photo? = null,
    val error: String? = null,
)
