package com.linkside.app.data.model

/** Monthly Invite Contest standings (friends who joined Linkside this month). */
data class ContestLeaderboard(
    val month: String,
    val prize: String,
    val daysLeft: Int = 0,
    val minJoinsToWin: Int = 10,
    val leaderboard: List<ContestEntry> = emptyList(),
    val myRank: Int? = null,
    val myCount: Int = 0,
)

data class ContestEntry(
    val rank: Int,
    val userId: String,
    val name: String,
    val count: Int,
    val isMe: Boolean = false,
)

data class ContestLeaderboardResponse(
    val ok: Boolean,
    val month: String? = null,
    val prize: String? = null,
    val daysLeft: Int = 0,
    val minJoinsToWin: Int = 10,
    val leaderboard: List<ContestEntry> = emptyList(),
    val myRank: Int? = null,
    val myCount: Int = 0,
    val error: String? = null,
)

/** Most recent contest win + claim / shipping status. */
data class ContestWin(
    val month: String,
    val prize: String,
    val inviteCount: Int = 0,
    val claimed: Boolean = false,
    val fulfilled: Boolean = false,
    val shippingName: String? = null,
    val shippingEmail: String? = null,
    val shippingAddress: String? = null,
    val shippingCity: String? = null,
    val shippingState: String? = null,
    val shippingZip: String? = null,
)

data class ContestClaimResponse(
    val ok: Boolean,
    val win: ContestWin? = null,
    val error: String? = null,
)

data class ContestClaimRequest(
    val month: String,
    val name: String,
    val email: String? = null,
    val address: String,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
)

data class ContestClaimSubmitResponse(
    val ok: Boolean,
    val claimed: Boolean = false,
    val error: String? = null,
)

/** Lightweight referral counts (tier rewards omitted for Android contest-only UI). */
data class ReferralSummary(
    val invitedCount: Int = 0,
    val joinedCount: Int = 0,
)

data class ReferralSummaryResponse(
    val ok: Boolean,
    val invitedCount: Int = 0,
    val joinedCount: Int = 0,
    val error: String? = null,
)
