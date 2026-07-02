package com.linkside.app.data.model

import com.squareup.moshi.Json

data class Friend(
    val phone: String,
    val firstName: String,
    val lastName: String = "",
) {
    val id: String get() = phone

    val fullName: String
        get() = listOf(firstName, lastName)
            .joinToString(" ")
            .trim()
            .ifEmpty { phone }

    val initials: String
        get() {
            val f = firstName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            val l = lastName.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            return (f + l).ifEmpty { "?" }
        }
}

data class FriendGroup(
    val id: String,
    var name: String,
    var members: List<Friend> = emptyList(),
)

data class FriendGroupResponse(
    val ok: Boolean,
    val groups: List<FriendGroup>? = null,
    val group: FriendGroup? = null,
    val error: String? = null,
)

data class GolfersResponse(
    val ok: Boolean,
    val golfers: List<Friend>? = null,
    val error: String? = null,
)

data class SaveGolfersRequest(val golfers: List<FriendPayload>)

data class FriendPayload(
    val phone: String,
    val firstName: String,
    val lastName: String = "",
)

data class CreateFriendGroupRequest(
    val name: String,
    val members: List<FriendPayload> = emptyList(),
)

data class UpdateFriendGroupRequest(
    val name: String,
    val members: List<FriendPayload>,
)

data class ContactStatusPayload(
    val registered: Boolean,
    val optedIn: Boolean,
)

data class ContactStatusResponse(
    val ok: Boolean,
    val statuses: Map<String, ContactStatusPayload> = emptyMap(),
)

data class ContactStatus(
    val registered: Boolean,
    val optedIn: Boolean,
) {
    val label: String
        get() = when {
            registered -> "In App"
            optedIn -> "Opted In"
            else -> "Not Yet"
        }
}

fun Friend.toPayload(): FriendPayload = FriendPayload(
    phone = phone,
    firstName = firstName,
    lastName = lastName,
)
