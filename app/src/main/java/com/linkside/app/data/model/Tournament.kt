package com.linkside.app.data.model

import com.squareup.moshi.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Tournament(
    val id: String,
    val name: String,
    val date: Long? = null,
    val startTime: String? = null,
    val courseName: String,
    val courseId: String? = null,
    val format: String? = null,
    val handicapFormat: String? = null,
    val maxPlayers: Int? = null,
    val entryFee: Double? = null,
    val paymentMode: String? = null,
    val creatorId: String? = null,
    val notes: String? = null,
    val status: String = "draft",
    val createdAt: Long? = null,
    val filledSpots: Int? = null,
    val myParticipantStatus: String? = null,
) {
    val isOpen: Boolean get() = status == "open"
    val isRegistered: Boolean
        get() = myParticipantStatus in setOf("registered", "checked_in", "waitlist")
    val isWithdrawn: Boolean get() = myParticipantStatus == "withdrawn"

    /** Human-readable play format (e.g. "Scramble"), matching iOS TournamentFormat.displayName. */
    val formatLabel: String?
        get() = when (format) {
            "stroke_play" -> "Stroke Play"
            "scramble" -> "Scramble"
            "best_ball" -> "Best Ball"
            "match_play" -> "Match Play"
            "skins" -> "Skins"
            else -> format?.takeIf { it.isNotBlank() }
                ?.replace('_', ' ')
                ?.replaceFirstChar { it.uppercase() }
        }

    /** Human-readable handicap format, matching iOS HandicapFormat.displayName. */
    val handicapLabel: String?
        get() = when (handicapFormat) {
            "none" -> "No Handicap"
            "full" -> "Full Handicap"
            "partial" -> "Partial Handicap"
            else -> null
        }

    fun parsedInstant(): Instant? = date?.let { Instant.ofEpochMilli(it) }

    fun formattedDate(): String {
        val instant = parsedInstant() ?: return "Date TBA"
        val zoned = instant.atZone(ZoneId.systemDefault())
        val day = DateTimeFormatter.ofPattern("EEE • MMM d", Locale.getDefault()).format(zoned)
        return if (!startTime.isNullOrBlank()) "$day • $startTime" else day
    }
}

data class TournamentParticipant(
    val id: String,
    val tournamentId: String = "",
    val userId: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val name: String,
    val status: String = "invited",
    val handicap: Double? = null,
    val tee: String? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val groupName: String? = null,
    val cartNumber: Int? = null,
    val startingHole: Int? = null,
    val paymentStatus: String? = null,
) {
    val hasAssignment: Boolean
        get() = !teamName.isNullOrBlank() ||
            !groupName.isNullOrBlank() ||
            !tee.isNullOrBlank() ||
            cartNumber != null ||
            startingHole != null
}

data class TournamentProduct(
    val id: String,
    val tournamentId: String? = null,
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    @Json(name = "is_addon") val isAddon: Boolean? = null,
    @Json(name = "team_size") val teamSize: Int? = null,
)

data class TournamentsListResponse(
    val ok: Boolean,
    val tournaments: List<Tournament> = emptyList(),
    val error: String? = null,
)

data class TournamentDetailResponse(
    val ok: Boolean,
    val tournament: Tournament? = null,
    val participants: List<TournamentParticipant> = emptyList(),
    val error: String? = null,
)

data class TournamentProductsResponse(
    val ok: Boolean,
    val products: List<TournamentProduct> = emptyList(),
    val error: String? = null,
)

data class TournamentRegisterRequest(
    val productIds: List<String> = emptyList(),
    val teammates: List<Map<String, String>> = emptyList(),
    val groupName: String? = null,
    val teamName: String? = null,
    val paymentIntentId: String? = null,
)

data class TournamentRegisterResponse(
    val ok: Boolean,
    val participant: TournamentParticipant? = null,
    val teamId: String? = null,
    val waitlisted: Boolean = false,
    val error: String? = null,
)

/** Body for self RSVP updates (e.g. withdrawing) on a tournament participant. */
data class TournamentParticipantStatusRequest(
    val status: String,
)

data class TournamentParticipantResponse(
    val ok: Boolean,
    val participant: TournamentParticipant? = null,
    val error: String? = null,
)
