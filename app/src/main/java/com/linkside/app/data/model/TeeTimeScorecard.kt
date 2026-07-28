package com.linkside.app.data.model

import com.squareup.moshi.Json

data class ScorecardHole(
    val hole: Int,
    val par: Int? = null,
    val score: Int? = null,
    val handicap: Int? = null,
)

data class TeeTimeScorecard(
    val id: String,
    @Json(name = "tee_time_id") val teeTimeId: String = "",
    @Json(name = "uploaded_by") val uploadedBy: String = "",
    @Json(name = "player_name") val playerName: String? = null,
    val holes: List<ScorecardHole>? = null,
    val front9: Int? = null,
    val back9: Int? = null,
    val total: Int? = null,
    val source: String? = null,
)

data class TeeTimeScorecardsResponse(
    val ok: Boolean,
    val scorecards: List<TeeTimeScorecard> = emptyList(),
    val error: String? = null,
)

/** AI-generated shareable post-round blurb (GET /tee-times/:id/round-summary). */
data class RoundSummary(
    val blurb: String,
    val courseName: String,
    val hasScores: Boolean = false,
)

data class RoundSummaryResponse(
    val ok: Boolean,
    val blurb: String? = null,
    val courseName: String? = null,
    val hasScores: Boolean = false,
    val error: String? = null,
)

/** Ranked player for share cards (round results / Player of the Day). */
data class ScorecardShareEntry(
    val id: String,
    val name: String,
    val total: Int,
    val front9: Int? = null,
    val back9: Int? = null,
    val birdies: Int = 0,
    val eaglesOrBetter: Int = 0,
    val holesPlayed: Int = 0,
    val isComplete: Boolean = false,
    val toPar: Int? = null,
) {
    companion object {
        fun from(card: TeeTimeScorecard): ScorecardShareEntry {
            val scoredHoles = card.holes.orEmpty().filter { it.score != null }
            var birdies = 0
            var eagles = 0
            var toParAcc = 0
            var hasPar = false
            for (h in card.holes.orEmpty()) {
                val score = h.score ?: continue
                val par = h.par ?: continue
                val diff = score - par
                toParAcc += diff
                hasPar = true
                when (diff) {
                    -1 -> birdies++
                    else -> if (diff <= -2) eagles++
                }
            }
            val holeSum = scoredHoles.sumOf { it.score ?: 0 }
            return ScorecardShareEntry(
                id = card.id,
                name = card.playerName?.takeIf { it.isNotBlank() } ?: "Player",
                total = card.total ?: holeSum,
                front9 = card.front9,
                back9 = card.back9,
                birdies = birdies,
                eaglesOrBetter = eagles,
                holesPlayed = scoredHoles.size,
                isComplete = card.total != null,
                toPar = if (hasPar) toParAcc else null,
            )
        }

        fun rankedFrom(scorecards: List<TeeTimeScorecard>): List<ScorecardShareEntry> {
            val entries = scorecards
                .filter { !it.playerName.isNullOrBlank() && it.playerName != "_specs_" }
                .map { from(it) }
                .filter { it.isComplete || it.holesPlayed > 0 }
            val complete = entries.filter { it.isComplete }
            return if (complete.isNotEmpty()) {
                complete.sortedBy { it.total }
            } else {
                entries.sortedBy { it.toPar ?: it.total }
            }
        }
    }
}

data class ScoreboardRow(
    val id: String,
    val name: String,
    val total: Int,
    val holesPlayed: Int,
    val toPar: Int? = null,
    val isTeam: Boolean = false,
) {
    val toParLabel: String?
        get() = when (toPar) {
            null -> null
            0 -> "E"
            else -> if (toPar > 0) "+$toPar" else "$toPar"
        }
}

/** Format-aware leaderboard from tee-time scorecards (matches iOS ScoringEngine). */
object ScoringEngine {
    fun leaderboard(
        scorecards: List<TeeTimeScorecard>,
        playFormat: String?,
        holes: Int = 18,
        teamName: String? = null,
    ): List<ScoreboardRow> {
        val cards = playableCards(scorecards)
        if (cards.isEmpty()) return emptyList()

        return when (playFormat) {
            PlayFormat.SCRAMBLE.raw, PlayFormat.BEST_BALL.raw -> {
                var holesPlayed = 0
                var teamTotal = 0
                for (h in 1..holes) {
                    val scores = cards.mapNotNull { card ->
                        card.holes?.firstOrNull { it.hole == h }?.score?.takeIf { it > 0 }
                    }
                    val best = scores.minOrNull()
                    if (best != null) {
                        teamTotal += best
                        holesPlayed += 1
                    }
                }
                if (teamTotal == 0) {
                    teamTotal = cards.minOfOrNull(::cardTotal) ?: 0
                }
                val par = parSum(cards, holes)
                val label = teamName?.takeIf { it.isNotBlank() }
                    ?: if (playFormat == PlayFormat.SCRAMBLE.raw) "Team (Scramble)" else "Team (Best Ball)"
                listOf(
                    ScoreboardRow(
                        id = "team",
                        name = label,
                        total = teamTotal,
                        holesPlayed = holesPlayed,
                        toPar = par?.let { teamTotal - it },
                        isTeam = true,
                    ),
                )
            }
            else -> {
                val par = parSum(cards, holes)
                cards.map { card ->
                    val total = cardTotal(card)
                    val played = card.holes?.count { (it.score ?: 0) > 0 } ?: holes
                    ScoreboardRow(
                        id = card.id,
                        name = card.playerName?.takeIf { it.isNotBlank() } ?: "Player",
                        total = total,
                        holesPlayed = played,
                        toPar = par?.let { total - it },
                        isTeam = false,
                    )
                }.sortedBy { it.total }
            }
        }
    }

    private fun playableCards(cards: List<TeeTimeScorecard>): List<TeeTimeScorecard> =
        cards.filter { it.source == "manual" && it.playerName != "_specs_" }
            .filter { card ->
                card.holes?.any { (it.score ?: 0) > 0 } == true || (card.total ?: 0) > 0
            }

    private fun cardTotal(card: TeeTimeScorecard): Int {
        val holeSum = card.holes?.mapNotNull { it.score }?.sum() ?: 0
        return card.total ?: holeSum
    }

    private fun parSum(cards: List<TeeTimeScorecard>, holes: Int): Int? {
        for (card in cards) {
            val pars = card.holes.orEmpty().take(holes).mapNotNull { it.par }
            if (pars.size >= holes) return pars.sum()
        }
        return null
    }
}
