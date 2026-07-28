package com.linkside.app.data.model

import com.squareup.moshi.Json

data class Friend(
    val phone: String,
    val firstName: String,
    val lastName: String = "",
    /**
     * Device-contact phone-type label (e.g. "Mobile", "Home", "Work"). Only set
     * when a single contact has more than one number, so the picker can tell them
     * apart. Not persisted to the server.
     */
    val phoneLabel: String? = null,
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
    /** Registered app user or SMS-opted-in — same as iOS `canReceiveAutoSMS` / golfers "On Linkside". */
    val isOnLinkside: Boolean get() = registered || optedIn

    val label: String
        get() = when {
            registered -> "In App"
            optedIn -> "Opted In"
            else -> "Not Yet"
        }
}

data class OptInMessageRequest(
    val phone: String,
    val name: String,
    val hostName: String? = null,
)

data class ManualInvite(
    val phone: String,
    val name: String,
    val optInLink: String = "",
    val message: String,
    val ok: Boolean = true,
    val error: String? = null,
)

fun Friend.toPayload(): FriendPayload = FriendPayload(
    phone = phone,
    firstName = firstName,
    lastName = lastName,
)
