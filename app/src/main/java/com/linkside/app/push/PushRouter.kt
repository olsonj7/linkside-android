package com.linkside.app.push

import android.content.Intent

/** Deep-link target parsed from a push notification tap or FCM data payload. */
sealed class PushRoute {
    data class RoundSummary(val teeTimeId: String) : PushRoute()
    data class PlayerOfTheDay(val teeTimeId: String) : PushRoute()
    data object ContestClaim : PushRoute()
    data class TeeTime(val id: String) : PushRoute()
    data class Trip(val id: String) : PushRoute()
    data class Tournament(val id: String, val invite: Boolean = false) : PushRoute()
    data class IdeaThread(val id: String) : PushRoute()
}

object PushRouter {
    private val lock = Any()
    private var pending: PushRoute? = null
    private val listeners = mutableListOf<(PushRoute) -> Unit>()

    fun publish(route: PushRoute) {
        synchronized(lock) {
            pending = route
            listeners.toList().forEach { it(route) }
        }
    }

    fun consumePending(): PushRoute? = synchronized(lock) {
        pending.also { pending = null }
    }

    fun addListener(listener: (PushRoute) -> Unit) {
        synchronized(lock) {
            listeners.add(listener)
            pending?.let(listener)
        }
    }

    fun removeListener(listener: (PushRoute) -> Unit) {
        synchronized(lock) {
            listeners.remove(listener)
        }
    }
}

object PushIntentParser {
    fun parse(intent: Intent?): PushRoute? {
        if (intent == null) return null
        if (intent.getBooleanExtra(EXTRA_CONTEST_WINNER, false)) {
            return PushRoute.ContestClaim
        }
        val teeTimeId = intent.getStringExtra(EXTRA_TEE_TIME_ID)
        if (!teeTimeId.isNullOrBlank()) {
            val playerOfTheDay = intent.getBooleanExtra(EXTRA_PLAYER_OF_THE_DAY, false)
            val roundRecap = intent.getBooleanExtra(EXTRA_ROUND_RECAP, false)
            return when {
                playerOfTheDay || roundRecap -> PushRoute.RoundSummary(teeTimeId)
                else -> PushRoute.TeeTime(teeTimeId)
            }
        }
        intent.getStringExtra(EXTRA_TRIP_ID)?.takeIf { it.isNotBlank() }?.let {
            return PushRoute.Trip(it)
        }
        intent.getStringExtra(EXTRA_TOURNAMENT_ID)?.takeIf { it.isNotBlank() }?.let {
            val invite = intent.getBooleanExtra(EXTRA_TOURNAMENT_INVITE, false)
            return PushRoute.Tournament(it, invite)
        }
        intent.getStringExtra(EXTRA_IDEA_THREAD_ID)?.takeIf { it.isNotBlank() }?.let {
            return PushRoute.IdeaThread(it)
        }
        return null
    }

    fun parseData(data: Map<String, String>): PushRoute? {
        val contestWinner = data["contestWinner"] == "true" || data["contestWinner"] == "1"
        if (contestWinner) return PushRoute.ContestClaim
        val teeTimeId = data["teeTimeId"]
        if (!teeTimeId.isNullOrBlank()) {
            val playerOfTheDay = data["playerOfTheDay"] == "true" || data["playerOfTheDay"] == "1"
            val roundRecap = data["roundRecap"] == "true" || data["roundRecap"] == "1"
            return when {
                playerOfTheDay || roundRecap -> PushRoute.RoundSummary(teeTimeId)
                else -> PushRoute.TeeTime(teeTimeId)
            }
        }
        data["tripId"]?.takeIf { it.isNotBlank() }?.let { return PushRoute.Trip(it) }
        data["tournamentId"]?.takeIf { it.isNotBlank() }?.let {
            val invite = data["tournamentInvite"] == "true" || data["tournamentInvite"] == "1"
            return PushRoute.Tournament(it, invite)
        }
        data["ideaThreadId"]?.takeIf { it.isNotBlank() }?.let { return PushRoute.IdeaThread(it) }
        return null
    }

    fun applyToIntent(intent: Intent, route: PushRoute) {
        intent.removeExtra(EXTRA_TEE_TIME_ID)
        intent.removeExtra(EXTRA_ROUND_RECAP)
        intent.removeExtra(EXTRA_PLAYER_OF_THE_DAY)
        intent.removeExtra(EXTRA_CONTEST_WINNER)
        intent.removeExtra(EXTRA_TRIP_ID)
        intent.removeExtra(EXTRA_TOURNAMENT_ID)
        intent.removeExtra(EXTRA_TOURNAMENT_INVITE)
        intent.removeExtra(EXTRA_IDEA_THREAD_ID)
        when (route) {
            is PushRoute.RoundSummary -> {
                intent.putExtra(EXTRA_TEE_TIME_ID, route.teeTimeId)
                intent.putExtra(EXTRA_ROUND_RECAP, true)
            }
            is PushRoute.PlayerOfTheDay -> {
                intent.putExtra(EXTRA_TEE_TIME_ID, route.teeTimeId)
                intent.putExtra(EXTRA_PLAYER_OF_THE_DAY, true)
            }
            is PushRoute.ContestClaim -> intent.putExtra(EXTRA_CONTEST_WINNER, true)
            is PushRoute.TeeTime -> intent.putExtra(EXTRA_TEE_TIME_ID, route.id)
            is PushRoute.Trip -> intent.putExtra(EXTRA_TRIP_ID, route.id)
            is PushRoute.Tournament -> {
                intent.putExtra(EXTRA_TOURNAMENT_ID, route.id)
                intent.putExtra(EXTRA_TOURNAMENT_INVITE, route.invite)
            }
            is PushRoute.IdeaThread -> intent.putExtra(EXTRA_IDEA_THREAD_ID, route.id)
        }
    }

    const val EXTRA_TEE_TIME_ID = "teeTimeId"
    const val EXTRA_ROUND_RECAP = "roundRecap"
    const val EXTRA_PLAYER_OF_THE_DAY = "playerOfTheDay"
    const val EXTRA_CONTEST_WINNER = "contestWinner"
    const val EXTRA_TRIP_ID = "tripId"
    const val EXTRA_TOURNAMENT_ID = "tournamentId"
    const val EXTRA_TOURNAMENT_INVITE = "tournamentInvite"
    const val EXTRA_IDEA_THREAD_ID = "ideaThreadId"
}
