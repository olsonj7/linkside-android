package com.linkside.app.data.model

import com.squareup.moshi.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class InviteStatus(val raw: String) {
    WAITING("waiting"),
    YES("yes"),
    NO("no"),
    MAYBE("maybe"),
    ;

    companion object {
        fun fromRaw(value: String?): InviteStatus =
            entries.firstOrNull { it.raw == value } ?: WAITING
    }
}

data class Invite(
    val phone: String? = null,
    val name: String,
    val status: String = InviteStatus.WAITING.raw,
    val isHost: Boolean? = null,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "can_invite") val canInvite: Boolean? = null,
    @Json(name = "deposit_paid") val depositPaid: Boolean? = null,
    @Json(name = "balance_paid") val balancePaid: Boolean? = null,
    @Json(name = "time_mode") val timeMode: String? = null,
    @Json(name = "time_windows") val timeWindows: List<String>? = null,
    /** false = saved without inviting; null/true = already notified (or legacy). */
    val notified: Boolean? = null,
) {
    val inviteStatus: InviteStatus get() = InviteStatus.fromRaw(status)

    fun matchesUser(user: User?): Boolean {
        if (user == null) return false
        if (userId != null && userId == user.id) return true
        val userPhone = user.phone?.let { com.linkside.app.data.api.PhoneUtils.normalizePhone(it) }
        val invitePhone = phone?.let { com.linkside.app.data.api.PhoneUtils.normalizePhone(it) }
        return userPhone != null && invitePhone != null && userPhone == invitePhone
    }
}

data class TeeTime(
    val id: String,
    val courseName: String,
    val courseId: String? = null,
    val date: String,
    val golfersNeeded: Int,
    val creatorId: String,
    val invites: List<Invite> = emptyList(),
    val tripId: String? = null,
    val playFormat: String? = null,
    val greenFee: Double? = null,
    val holesCount: Int? = null,
    val teamName: String? = null,
    /** Optional label like "Father's Day Round" or "Birthday Round". */
    val roundName: String? = null,
    /** Auto-discovered tee-sheet booking URL (GolfNow / ForeUp / etc.). */
    val bookingUrl: String? = null,
) {
    val yesCount: Int get() = invites.count { it.inviteStatus == InviteStatus.YES }
    val maybeCount: Int get() = invites.count { it.inviteStatus == InviteStatus.MAYBE }
    val waitingCount: Int get() = invites.count { it.inviteStatus == InviteStatus.WAITING }
    val noCount: Int get() = invites.count { it.inviteStatus == InviteStatus.NO }
    val isFull: Boolean get() = yesCount >= golfersNeeded

    val hostInvite: Invite? get() = invites.firstOrNull { it.isHost == true }

    /** Golfers saved via "Save without inviting" who haven't been notified yet. */
    val pendingInvites: List<Invite>
        get() = invites.filter { it.isHost != true && it.notified == false }

    val hasPendingInvites: Boolean get() = pendingInvites.isNotEmpty()

    val timeMode: String
        get() = hostInvite?.timeMode?.takeIf { it == "flexible" } ?: "specific"

    val timeWindows: List<String>
        get() = hostInvite?.timeWindows.orEmpty()

    val isFlexibleTime: Boolean get() = timeMode == "flexible"

    fun myInvite(user: User?): Invite? = invites.firstOrNull { it.matchesUser(user) }

    fun myInviteStatus(user: User?): InviteStatus? = myInvite(user)?.inviteStatus

    fun isDeclinedBy(user: User?): Boolean = myInviteStatus(user) == InviteStatus.NO

    /** Keep declined tee times visible for 24 hours after tee time (matches iOS). */
    fun isActiveDeclined(user: User?, now: Instant = Instant.now()): Boolean {
        if (!isDeclinedBy(user)) return false
        val date = parsedInstant() ?: return false
        return date.isAfter(now.minusSeconds(24 * 3600))
    }

    fun parsedInstant(): Instant? = TeeTimeDates.parse(date)

    fun isPast(now: Instant = Instant.now()): Boolean {
        val instant = parsedInstant() ?: return false
        return instant.isBefore(now)
    }

    /** Local RSVP patch used for optimistic UI before the server round-trip completes. */
    fun withInviteStatusFor(
        user: User?,
        phone: String?,
        userId: String?,
        status: InviteStatus,
    ): TeeTime {
        val normalizedPhone = phone?.let { com.linkside.app.data.api.PhoneUtils.normalizePhone(it) }
        return copy(
            invites = invites.map { invite ->
                val matchesUser = invite.matchesUser(user)
                val matchesPhone = normalizedPhone != null &&
                    invite.phone?.let { com.linkside.app.data.api.PhoneUtils.normalizePhone(it) } == normalizedPhone
                val matchesId = !userId.isNullOrBlank() && invite.userId == userId
                if (matchesUser || matchesPhone || matchesId) invite.copy(status = status.raw) else invite
            },
        )
    }

    fun formattedDate(): String {
        val instant = parsedInstant() ?: return date
        val zoned = instant.atZone(ZoneId.systemDefault())
        if (isFlexibleTime) {
            val day = DateTimeFormatter.ofPattern("EEE • MMM d", Locale.getDefault()).format(zoned)
            val windows = timeWindows.mapNotNull { raw ->
                TeeTimeWindow.entries.firstOrNull { it.raw == raw }?.label
            }
            val label = when {
                windows.isEmpty() || windows.any { it == TeeTimeWindow.ANY.label } -> TeeTimeWindow.ANY.label
                else -> windows.joinToString(" / ")
            }
            return "$day • $label"
        }
        val formatter = DateTimeFormatter.ofPattern("EEE • MMM d • h:mm a", Locale.getDefault())
        return formatter.format(zoned)
    }
}

object TeeTimeDates {
    fun parse(raw: String): Instant? {
        return try {
            Instant.parse(raw)
        } catch (_: Exception) {
            try {
                Instant.ofEpochMilli(raw.toLong())
            } catch (_: Exception) {
                null
            }
        }
    }

    fun format(instant: Instant): String = instant.toString()
}

data class TeeTimeListResponse(
    val ok: Boolean,
    val teeTimes: List<TeeTime> = emptyList(),
)

data class TeeTimeResponse(
    val ok: Boolean,
    val teeTime: TeeTime? = null,
    val error: String? = null,
    val manualInvites: List<ManualInvite> = emptyList(),
)

enum class PlayFormat(val raw: String, val displayName: String) {
    STROKE_PLAY("stroke_play", "Stroke Play"),
    SCRAMBLE("scramble", "Scramble"),
    BEST_BALL("best_ball", "Best Ball"),
}

enum class TeeTimeWindow(val raw: String, val label: String) {
    ANY("any", "Any time"),
    MORNING("morning", "Morning"),
    MIDDAY("midday", "Mid Day"),
    AFTERNOON("afternoon", "Afternoon"),
    TWILIGHT("twilight", "Twilight"),
}

data class CreateTeeTimeRequest(
    val courseName: String,
    val courseId: String = "",
    val date: String,
    val golfersNeeded: Int,
    val invites: List<InvitePayload>,
    val timeMode: String = "specific",
    val timeWindows: List<String> = emptyList(),
    val playFormat: String? = null,
    val greenFee: Double? = null,
    val holesCount: Int = 18,
    val roundName: String? = null,
    /** false = save as draft; invitees get notified:false until send-invites. */
    val sendInvites: Boolean = true,
)

data class SendInvitesResponse(
    val ok: Boolean,
    val teeTime: TeeTime? = null,
    val sent: Int = 0,
    val manualInvites: List<ManualInvite> = emptyList(),
    val error: String? = null,
)

data class UpdateTeeTimeRequest(
    val date: String,
    val golfersNeeded: Int,
    val timeMode: String,
    val timeWindows: List<String> = emptyList(),
    val playFormat: String? = null,
    val greenFee: Double? = null,
    val holesCount: Int? = null,
    val roundName: String? = null,
)

data class InvitePayload(
    val phone: String,
    val name: String,
)

data class UpdateInviteStatusRequest(
    val status: String,
    val phone: String? = null,
    val userId: String? = null,
)

data class AddInvitesRequest(
    val invites: List<InvitePayload>,
    /** false when editing a draft that still has pending invites. */
    val notify: Boolean = true,
)

data class AddInvitesResponse(
    val ok: Boolean,
    val teeTime: TeeTime? = null,
    val added: Int = 0,
    val manualInvites: List<ManualInvite> = emptyList(),
    val error: String? = null,
)

data class RemoveInviteRequest(
    val userId: String? = null,
    val phone: String? = null,
)

/** Bump a specific invitee for their RSVP. Linkside users are notified in-app; SMS-only handled client-side. */
data class BumpInviteeRequest(
    val userId: String? = null,
    val phone: String? = null,
)

/** Toggle whether a Linkside invitee can invite others to this tee time (creator-only). */
data class ToggleInviteAccessRequest(
    val userId: String,
)

data class DeleteTeeTimeResponse(
    val ok: Boolean,
    val deletedId: String? = null,
    val notified: Int? = null,
    val error: String? = null,
)

data class GolfCourse(
    val name: String,
    val address: String? = null,
    @Json(name = "place_id") val placeId: String,
)

data class CourseSearchResponse(
    val ok: Boolean,
    val courses: List<GolfCourse> = emptyList(),
)

data class CourseWebsiteResponse(
    val ok: Boolean,
    val website: String? = null,
    val error: String? = null,
)
