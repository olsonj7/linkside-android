package com.linkside.app.data.repository

import com.linkside.app.data.api.LinksideApi
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.data.api.runApi
import com.linkside.app.data.model.ContactStatus
import com.linkside.app.data.model.ContactStatusRequest
import com.linkside.app.data.model.CreateFriendGroupRequest
import com.linkside.app.data.model.CreateTeeTimeRequest
import com.linkside.app.data.model.Friend
import com.linkside.app.data.model.FriendGroup
import com.linkside.app.data.model.FriendPayload
import com.linkside.app.data.model.GolfCourse
import com.linkside.app.data.model.GolfTrip
import com.linkside.app.data.model.InvitePayload
import com.linkside.app.data.model.Photo
import com.linkside.app.data.model.SaveGolfersRequest
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TeeTimeDates
import com.linkside.app.data.model.TripChatMessage
import com.linkside.app.data.model.SendTripMessageRequest
import com.linkside.app.data.model.TripPaymentRequest
import com.linkside.app.data.model.TripRsvpRequest
import com.linkside.app.data.model.UpdateFriendGroupRequest
import com.linkside.app.data.model.UpdateInviteStatusRequest
import com.linkside.app.data.model.toPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant

class LinksideRepository(
    private val api: LinksideApi,
) {
    suspend fun fetchSavedGolfers(): List<Friend> {
        val response = runApi { api.fetchSavedGolfers() }
        if (!response.ok) throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to fetch golfers")
        return response.golfers.orEmpty()
    }

    suspend fun saveGolfers(golfers: List<Friend>) {
        runApi {
            api.saveGolfers(
                SaveGolfersRequest(golfers.map { it.toPayload().copy(phone = PhoneUtils.normalizePhone(it.phone)) }),
            )
        }
    }

    suspend fun fetchFriendGroups(): List<FriendGroup> {
        val response = runApi { api.fetchFriendGroups() }
        if (!response.ok) throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to fetch groups")
        return response.groups.orEmpty()
    }

    suspend fun createFriendGroup(name: String, members: List<Friend>): FriendGroup {
        val response = runApi {
            api.createFriendGroup(
                CreateFriendGroupRequest(
                    name = name.trim(),
                    members = members.map { it.toPayload().copy(phone = PhoneUtils.normalizePhone(it.phone)) },
                ),
            )
        }
        if (!response.ok || response.group == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to create group")
        }
        return response.group
    }

    suspend fun updateFriendGroup(group: FriendGroup): FriendGroup {
        val response = runApi {
            api.updateFriendGroup(
                group.id,
                UpdateFriendGroupRequest(
                    name = group.name.trim(),
                    members = group.members.map { it.toPayload().copy(phone = PhoneUtils.normalizePhone(it.phone)) },
                ),
            )
        }
        if (!response.ok || response.group == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to update group")
        }
        return response.group
    }

    suspend fun deleteFriendGroup(id: String) {
        runApi { api.deleteFriendGroup(id) }
    }

    suspend fun checkContactStatuses(phones: List<String>): Map<String, ContactStatus> {
        if (phones.isEmpty()) return emptyMap()
        val normalized = phones.map { PhoneUtils.normalizePhone(it) }
        val response = runApi { api.checkContactStatuses(ContactStatusRequest(normalized)) }
        return response.statuses.mapValues { (_, value) ->
            ContactStatus(registered = value.registered, optedIn = value.optedIn)
        }
    }

    suspend fun fetchTeeTimes(): List<TeeTime> {
        val response = runApi { api.fetchTeeTimes() }
        if (!response.ok) throw com.linkside.app.data.api.ApiException("Failed to fetch tee times")
        return response.teeTimes.sortedBy { it.parsedInstant() ?: Instant.EPOCH }
    }

    suspend fun fetchTeeTime(id: String): TeeTime {
        val response = runApi { api.fetchTeeTime(id) }
        if (!response.ok || response.teeTime == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to fetch tee time")
        }
        return response.teeTime
    }

    suspend fun createTeeTime(
        courseName: String,
        courseId: String?,
        date: Instant,
        golfersNeeded: Int,
        invites: List<Friend>,
        timeMode: String = "specific",
        timeWindows: List<String> = emptyList(),
        playFormat: String? = null,
        greenFee: Double? = null,
    ): TeeTime {
        val response = runApi {
            api.createTeeTime(
                CreateTeeTimeRequest(
                    courseName = courseName,
                    courseId = courseId.orEmpty(),
                    date = TeeTimeDates.format(date),
                    golfersNeeded = golfersNeeded,
                    invites = invites.map {
                        InvitePayload(
                            phone = PhoneUtils.normalizePhone(it.phone),
                            name = it.fullName,
                        )
                    },
                    timeMode = timeMode,
                    timeWindows = timeWindows,
                    playFormat = playFormat,
                    greenFee = greenFee,
                ),
            )
        }
        if (!response.ok || response.teeTime == null) {
            throw com.linkside.app.data.api.ApiException("Failed to create tee time")
        }
        return response.teeTime
    }

    suspend fun updateInviteStatus(teeTimeId: String, phone: String?, status: String): TeeTime {
        val response = runApi {
            api.updateInviteStatus(
                teeTimeId,
                UpdateInviteStatusRequest(status = status, phone = phone?.let(PhoneUtils::normalizePhone)),
            )
        }
        if (!response.ok || response.teeTime == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to update RSVP")
        }
        return response.teeTime
    }

    suspend fun searchCourses(query: String): List<GolfCourse> {
        val response = runApi { api.searchCourses(query) }
        if (!response.ok) return emptyList()
        return response.courses
    }

    suspend fun fetchGolfTrips(): List<GolfTrip> {
        val response = runApi { api.fetchGolfTrips() }
        if (!response.ok) throw com.linkside.app.data.api.ApiException("Failed to fetch trips")
        return response.trips.sortedBy { it.parsedStart() ?: Instant.EPOCH }
    }

    suspend fun fetchGolfTrip(id: String): GolfTrip {
        val response = runApi { api.fetchGolfTrip(id) }
        if (!response.ok || response.trip == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to fetch trip")
        }
        return response.trip
    }

    suspend fun rsvpGolfTrip(tripId: String, status: String): GolfTrip {
        val response = runApi { api.rsvpGolfTrip(tripId, TripRsvpRequest(status)) }
        if (!response.ok || response.trip == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to update RSVP")
        }
        return response.trip
    }

    suspend fun setTripDepositPaid(tripId: String, phone: String?, userId: String?, paid: Boolean): GolfTrip {
        val response = runApi {
            api.setTripDepositPaid(
                tripId,
                TripPaymentRequest(
                    paid = paid,
                    phone = phone?.let(PhoneUtils::normalizePhone),
                    userId = userId,
                ),
            )
        }
        if (!response.ok || response.trip == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to update deposit")
        }
        return response.trip
    }

    suspend fun setTripBalancePaid(tripId: String, phone: String?, userId: String?, paid: Boolean): GolfTrip {
        val response = runApi {
            api.setTripBalancePaid(
                tripId,
                TripPaymentRequest(
                    paid = paid,
                    phone = phone?.let(PhoneUtils::normalizePhone),
                    userId = userId,
                ),
            )
        }
        if (!response.ok || response.trip == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to update balance")
        }
        return response.trip
    }

    suspend fun fetchTripTeeTimes(tripId: String): List<TeeTime> {
        val response = runApi { api.fetchTripTeeTimes(tripId) }
        if (!response.ok) return emptyList()
        return response.teeTimes.sortedBy { it.parsedInstant() ?: Instant.EPOCH }
    }

    suspend fun fetchTripMessages(tripId: String): List<TripChatMessage> {
        val response = runApi { api.fetchTripMessages(tripId) }
        if (!response.ok) return emptyList()
        return response.messages
    }

    suspend fun sendTripMessage(tripId: String, text: String): TripChatMessage {
        val response = runApi { api.sendTripMessage(tripId, SendTripMessageRequest(text.trim())) }
        if (!response.ok || response.message == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to send message")
        }
        return response.message
    }

    suspend fun fetchTripPhotos(tripId: String): List<Photo> {
        val response = runApi { api.fetchTripPhotos(tripId) }
        if (!response.ok) return emptyList()
        return response.photos
    }

    suspend fun uploadTripPhoto(tripId: String, imageBytes: ByteArray, mimeType: String): Photo {
        val ext = if (mimeType == "image/png") "png" else "jpg"
        val body = imageBytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("photo", "photo.$ext", body)
        val response = runApi { api.uploadTripPhoto(tripId, part) }
        if (!response.ok || response.photo == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to upload photo")
        }
        return response.photo
    }
}
