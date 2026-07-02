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
) {
    val yesCount: Int get() = invites.count { it.inviteStatus == InviteStatus.YES }
    val maybeCount: Int get() = invites.count { it.inviteStatus == InviteStatus.MAYBE }
    val waitingCount: Int get() = invites.count { it.inviteStatus == InviteStatus.WAITING }
    val noCount: Int get() = invites.count { it.inviteStatus == InviteStatus.NO }
    val isFull: Boolean get() = yesCount >= golfersNeeded

    fun myInvite(user: User?): Invite? = invites.firstOrNull { it.matchesUser(user) }

    fun myInviteStatus(user: User?): InviteStatus? = myInvite(user)?.inviteStatus

    fun isDeclinedBy(user: User?): Boolean = myInviteStatus(user) == InviteStatus.NO

    fun parsedInstant(): Instant? = TeeTimeDates.parse(date)

    fun formattedDate(): String {
        val instant = parsedInstant() ?: return date
        val zoned = instant.atZone(ZoneId.systemDefault())
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
)

data class InvitePayload(
    val phone: String,
    val name: String,
)

data class UpdateInviteStatusRequest(
    val status: String,
    val phone: String? = null,
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
