package com.linkside.app.ui.navigation

object Routes {
    const val Welcome = "welcome"
    const val PhoneLogin = "phone_login"
    const val EmailAuth = "email_auth"
    const val VerifyCode = "verify_code/{phone}"
    const val Onboarding = "onboarding"

    const val HomeMain = "home_main"
    const val CreateTeeTime = "create_tee_time"
    const val TeeTimeDetail = "tee_time_detail/{id}"
    const val TripDetail = "trip_detail/{id}"
    const val TripChat = "trip_chat/{id}"
    const val FriendGroups = "friend_groups"
    const val EditGroup = "edit_group?id={id}"
    const val GolferPicker = "golfer_picker"

    fun verifyCode(phone: String): String = "verify_code/${phone.encodeRoute()}"
    fun teeTimeDetail(id: String): String = "tee_time_detail/${id.encodeRoute()}"
    fun tripDetail(id: String): String = "trip_detail/${id.encodeRoute()}"
    fun tripChat(id: String): String = "trip_chat/${id.encodeRoute()}"
    fun editGroup(id: String?): String = if (id == null) "edit_group?id=" else "edit_group?id=${id.encodeRoute()}"
}

private fun String.encodeRoute(): String = java.net.URLEncoder.encode(this, Charsets.UTF_8.name())

fun String.decodeRoute(): String = java.net.URLDecoder.decode(this, Charsets.UTF_8.name())
