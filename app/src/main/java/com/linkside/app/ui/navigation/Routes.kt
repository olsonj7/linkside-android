package com.linkside.app.ui.navigation

object Routes {
    const val Welcome = "welcome"
    const val PhoneLogin = "phone_login"
    const val EmailAuth = "email_auth"
    const val VerifyCode = "verify_code/{phone}"
    const val Onboarding = "onboarding"

    const val HomeMain = "home_main"
    const val CreateTeeTime = "create_tee_time"
    const val EditTeeTime = "edit_tee_time/{id}"
    const val TeeTimeDetail = "tee_time_detail/{id}"
    const val TeeTimeChat = "tee_time_chat/{id}"
    const val TripDetail = "trip_detail/{id}"
    const val TripChat = "trip_chat/{id}"
    const val FriendGroups = "friend_groups"
    const val EditGroup = "edit_group/{id}"
    const val GolferPicker = "golfer_picker"
    const val IdeaThreads = "idea_threads"
    const val IdeaThreadDetail = "idea_thread_detail/{id}"
    const val CreateIdeaThread = "create_idea_thread"
    const val Notifications = "notifications"
    const val ManageInvitees = "manage_invitees/{id}"
    const val Scorecards = "scorecards/{id}"
    const val RoundSummary = "round_summary/{id}"
    const val PlayerOfTheDay = "player_of_the_day/{id}"
    const val Tournaments = "tournaments"
    const val TournamentDetail = "tournament_detail/{id}"
    const val ForgotPassword = "forgot_password"
    const val LinkEmail = "link_email"

    fun verifyCode(phone: String): String = "verify_code/${phone.encodeRoute()}"
    fun teeTimeDetail(id: String): String = "tee_time_detail/${id.encodeRoute()}"
    fun editTeeTime(id: String): String = "edit_tee_time/${id.encodeRoute()}"
    fun teeTimeChat(id: String): String = "tee_time_chat/${id.encodeRoute()}"
    fun tripDetail(id: String): String = "trip_detail/${id.encodeRoute()}"
    fun tripChat(id: String): String = "trip_chat/${id.encodeRoute()}"
    fun ideaThreadDetail(id: String): String = "idea_thread_detail/${id.encodeRoute()}"
    fun editGroup(id: String?): String = "edit_group/${(id ?: "new").encodeRoute()}"
    fun manageInvitees(id: String): String = "manage_invitees/${id.encodeRoute()}"
    fun scorecards(id: String): String = "scorecards/${id.encodeRoute()}"
    fun roundSummary(id: String): String = "round_summary/${id.encodeRoute()}"
    fun playerOfTheDay(id: String): String = "player_of_the_day/${id.encodeRoute()}"
    fun tournamentDetail(id: String): String = "tournament_detail/${id.encodeRoute()}"
}

private fun String.encodeRoute(): String = java.net.URLEncoder.encode(this, Charsets.UTF_8.name())

fun String.decodeRoute(): String = java.net.URLDecoder.decode(this, Charsets.UTF_8.name())
