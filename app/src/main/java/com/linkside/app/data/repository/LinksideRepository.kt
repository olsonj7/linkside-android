package com.linkside.app.data.repository

import com.linkside.app.data.api.LinksideApi
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.data.api.runApi
import com.linkside.app.data.model.AddInvitesRequest
import com.linkside.app.data.model.AddInvitesResponse
import com.linkside.app.data.model.ContestClaimRequest
import com.linkside.app.data.model.ContestLeaderboard
import com.linkside.app.data.model.ContestWin
import com.linkside.app.data.model.ContactStatus
import com.linkside.app.data.model.ContactStatusRequest
import com.linkside.app.data.model.CreateFriendGroupRequest
import com.linkside.app.data.model.CreateIdeaThreadRequest
import com.linkside.app.data.model.CreateTeeTimeRequest
import com.linkside.app.data.model.DeviceTokenRequest
import com.linkside.app.data.model.Friend
import com.linkside.app.data.model.FriendGroup
import com.linkside.app.data.model.FriendPayload
import com.linkside.app.data.model.GolfCourse
import com.linkside.app.data.model.GolfTrip
import com.linkside.app.data.model.IdeaMessage
import com.linkside.app.data.model.Poll
import com.linkside.app.data.model.IdeaThread
import com.linkside.app.data.model.InvitePayload
import com.linkside.app.data.model.ManualInvite
import com.linkside.app.data.model.AppNotification
import com.linkside.app.data.model.OptInMessageRequest
import com.linkside.app.data.model.Photo
import com.linkside.app.data.model.ReferralSummary
import com.linkside.app.data.model.RemoveInviteRequest
import com.linkside.app.data.model.RoundSummary
import com.linkside.app.data.model.SaveGolfersRequest
import com.linkside.app.data.model.SaveRoundScoreRequest
import com.linkside.app.data.model.SendInvitesResponse
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TeeTimeDates
import com.linkside.app.data.model.TeeTimeResponse
import com.linkside.app.data.model.TeeTimeChatMessage
import com.linkside.app.data.model.TeeTimeScorecard
import com.linkside.app.data.model.Tournament
import com.linkside.app.data.model.TournamentParticipant
import com.linkside.app.data.model.TournamentProduct
import com.linkside.app.data.model.TournamentRegisterRequest
import com.linkside.app.data.model.TripChatMessage
import com.linkside.app.data.model.SendIdeaMessageRequest
import com.linkside.app.data.model.SendTripMessageRequest
import com.linkside.app.data.model.SendTeeTimeMessageRequest
import com.linkside.app.data.model.MarkNotificationsReadRequest
import com.linkside.app.data.model.TripPaymentRequest
import com.linkside.app.data.model.TripRsvpRequest
import com.linkside.app.data.model.UpdateFriendGroupRequest
import com.linkside.app.data.model.UpdateInviteStatusRequest
import com.linkside.app.data.model.UpdateTeeTimeRequest
import com.linkside.app.data.model.WeatherFunSummaryRequest
import com.linkside.app.data.model.toPayload
import com.linkside.app.data.api.ApiException
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
                group.id.trim(),
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
        val response = runApi { api.deleteFriendGroup(id.trim()) }
        if (!response.ok) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to delete group")
        }
    }

    suspend fun checkContactStatuses(phones: List<String>): Map<String, ContactStatus> {
        if (phones.isEmpty()) return emptyMap()
        val normalized = phones.map { PhoneUtils.normalizePhone(it) }
        val response = runApi { api.checkContactStatuses(ContactStatusRequest(normalized)) }
        return response.statuses.mapValues { (_, value) ->
            ContactStatus(registered = value.registered, optedIn = value.optedIn)
        }
    }

    suspend fun getOptInMessage(phone: String, name: String, hostName: String?): ManualInvite {
        val response = runApi {
            api.getOptInMessage(
                OptInMessageRequest(
                    phone = PhoneUtils.normalizePhone(phone),
                    name = name,
                    hostName = hostName,
                ),
            )
        }
        if (!response.ok || response.message.isBlank()) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to prepare invite")
        }
        return response
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
        holesCount: Int = 18,
        roundName: String? = null,
        sendInvites: Boolean = true,
    ): TeeTimeResponse {
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
                    holesCount = holesCount,
                    roundName = roundName?.trim()?.takeIf { it.isNotEmpty() }?.take(60),
                    sendInvites = sendInvites,
                ),
            )
        }
        if (!response.ok || response.teeTime == null) {
            throw com.linkside.app.data.api.ApiException("Failed to create tee time")
        }
        return response
    }

    suspend fun updateTeeTime(
        id: String,
        date: Instant,
        golfersNeeded: Int,
        timeMode: String,
        timeWindows: List<String>,
        playFormat: String?,
        greenFee: Double?,
        holesCount: Int? = null,
        roundName: String? = null,
    ): TeeTime {
        val response = runApi {
            api.updateTeeTime(
                id,
                UpdateTeeTimeRequest(
                    date = TeeTimeDates.format(date),
                    golfersNeeded = golfersNeeded,
                    timeMode = timeMode,
                    timeWindows = timeWindows,
                    playFormat = playFormat,
                    greenFee = greenFee,
                    holesCount = holesCount,
                    roundName = roundName?.trim()?.take(60),
                ),
            )
        }
        if (!response.ok || response.teeTime == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to update tee time")
        }
        return response.teeTime
    }

    suspend fun deleteTeeTime(id: String) {
        val response = runApi { api.deleteTeeTime(id) }
        if (!response.ok) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to cancel tee time")
        }
    }

    suspend fun addTeeTimeInvites(
        teeTimeId: String,
        invites: List<Friend>,
        notify: Boolean = true,
    ): AddInvitesResponse {
        val response = runApi {
            api.addTeeTimeInvites(
                teeTimeId,
                AddInvitesRequest(
                    invites = invites.map {
                        InvitePayload(
                            phone = PhoneUtils.normalizePhone(it.phone),
                            name = it.fullName,
                        )
                    },
                    notify = notify,
                ),
            )
        }
        if (!response.ok) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to add invitees")
        }
        return response
    }

    /** Send invites for golfers saved via "Save without inviting". */
    suspend fun sendPendingInvites(teeTimeId: String): SendInvitesResponse {
        val response = runApi { api.sendPendingInvites(teeTimeId) }
        if (!response.ok || response.teeTime == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to send invites")
        }
        return response
    }

    suspend fun removeTeeTimeInvite(teeTimeId: String, userId: String?, phone: String?): TeeTime {
        val response = runApi {
            api.removeTeeTimeInvite(
                teeTimeId,
                RemoveInviteRequest(
                    userId = userId,
                    phone = phone?.let(PhoneUtils::normalizePhone),
                ),
            )
        }
        if (!response.ok || response.teeTime == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to remove invitee")
        }
        return response.teeTime
    }

    /** Bump a Linkside invitee for their RSVP (backend sends push + in-app notification). */
    suspend fun bumpInvitee(teeTimeId: String, userId: String?, phone: String?) {
        val response = runApi {
            api.bumpInvitee(
                teeTimeId,
                com.linkside.app.data.model.BumpInviteeRequest(
                    userId = userId,
                    phone = phone?.let(PhoneUtils::normalizePhone),
                ),
            )
        }
        if (!response.ok) {
            throw com.linkside.app.data.api.ApiException("Failed to send reminder")
        }
    }

    /** Toggle whether a Linkside invitee can invite others (creator-only). */
    suspend fun toggleInviteAccess(teeTimeId: String, userId: String): TeeTime {
        val response = runApi {
            api.toggleInviteAccess(
                teeTimeId,
                com.linkside.app.data.model.ToggleInviteAccessRequest(userId = userId),
            )
        }
        if (!response.ok || response.teeTime == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to update invite access")
        }
        return response.teeTime
    }

    suspend fun updateInviteStatus(
        teeTimeId: String,
        phone: String?,
        status: String,
        userId: String? = null,
    ): TeeTime {
        val response = runApi {
            api.updateInviteStatus(
                teeTimeId,
                UpdateInviteStatusRequest(
                    status = status,
                    phone = phone?.let(PhoneUtils::normalizePhone),
                    userId = userId,
                ),
            )
        }
        if (!response.ok || response.teeTime == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to update RSVP")
        }
        return response.teeTime
    }

    suspend fun searchCourses(
        query: String,
        lat: Double? = null,
        lng: Double? = null,
    ): List<GolfCourse> {
        val response = runApi { api.searchCourses(query, lat, lng) }
        if (!response.ok) return emptyList()
        return response.courses
    }

    suspend fun courseLocation(placeId: String?, name: String): Pair<Double, Double>? {
        if (placeId.isNullOrBlank() && name.isBlank()) return null
        return try {
            val response = runApi {
                if (!placeId.isNullOrBlank()) {
                    api.courseLocation(placeId = placeId, name = null)
                } else {
                    api.courseLocation(placeId = null, name = name)
                }
            }
            val lat = response.lat
            val lng = response.lng
            if (response.ok && lat != null && lng != null) lat to lng else null
        } catch (_: Exception) {
            null
        }
    }

    /** Course website from Google Places via `/courses/website`. Returns null if unknown. */
    suspend fun courseWebsite(placeId: String): String? {
        if (placeId.isBlank()) return null
        return try {
            val response = runApi { api.courseWebsite(placeId) }
            response.website?.takeIf { response.ok && it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun weatherFunSummary(request: WeatherFunSummaryRequest): String? {
        return try {
            val response = runApi { api.weatherFunSummary(request) }
            if (response.ok) response.summary?.takeIf { it.isNotBlank() } else null
        } catch (_: Exception) {
            null
        }
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

    suspend fun sendTripMessage(
        tripId: String,
        text: String,
        mentions: List<String> = emptyList(),
    ): TripChatMessage {
        val response = runApi { api.sendTripMessage(tripId, SendTripMessageRequest(text.trim(), mentions)) }
        if (!response.ok || response.message == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to send message")
        }
        return response.message
    }

    suspend fun toggleTripReaction(tripId: String, messageId: String, emoji: String): TripChatMessage {
        val response = runApi {
            api.toggleTripReaction(
                tripId,
                messageId,
                com.linkside.app.data.model.MessageReactionRequest(emoji),
            )
        }
        if (!response.ok || response.message == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to react")
        }
        return response.message
    }

    suspend fun fetchTripPhotos(tripId: String): List<Photo> {
        val response = runApi { api.fetchTripPhotos(tripId) }
        if (!response.ok) return emptyList()
        return response.photos
    }

    suspend fun fetchTripAnnouncements(tripId: String): List<com.linkside.app.data.model.TripAnnouncement> {
        val response = runApi { api.fetchTripAnnouncements(tripId) }
        if (!response.ok) return emptyList()
        return response.announcements
    }

    suspend fun postTripAnnouncement(
        tripId: String,
        message: String,
    ): com.linkside.app.data.model.TripAnnouncement {
        val response = runApi {
            api.postTripAnnouncement(
                tripId,
                com.linkside.app.data.model.PostAnnouncementRequest(message.trim()),
            )
        }
        if (!response.ok || response.announcement == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to post announcement")
        }
        return response.announcement
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

    suspend fun fetchTeeTimeMessages(teeTimeId: String): List<TeeTimeChatMessage> {
        val response = runApi { api.fetchTeeTimeMessages(teeTimeId) }
        if (!response.ok) return emptyList()
        return response.messages
    }

    suspend fun sendTeeTimeMessage(
        teeTimeId: String,
        text: String,
        mentions: List<String> = emptyList(),
    ): TeeTimeChatMessage {
        val response = runApi { api.sendTeeTimeMessage(teeTimeId, SendTeeTimeMessageRequest(text.trim(), mentions)) }
        if (!response.ok || response.message == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to send message")
        }
        return response.message
    }

    suspend fun toggleTeeTimeReaction(teeTimeId: String, messageId: String, emoji: String): TeeTimeChatMessage {
        val response = runApi {
            api.toggleTeeTimeReaction(
                teeTimeId,
                messageId,
                com.linkside.app.data.model.MessageReactionRequest(emoji),
            )
        }
        if (!response.ok || response.message == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to react")
        }
        return response.message
    }

    // POLLS

    suspend fun createTripPoll(
        tripId: String,
        question: String,
        options: List<String>,
        allowMultiple: Boolean,
    ): TripChatMessage {
        val response = runApi {
            api.createTripPoll(
                tripId,
                com.linkside.app.data.model.CreatePollRequest(question.trim(), options, allowMultiple),
            )
        }
        if (!response.ok || response.message == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to create poll")
        }
        return response.message
    }

    suspend fun createIdeaThreadPoll(
        threadId: String,
        question: String,
        options: List<String>,
        allowMultiple: Boolean,
    ): IdeaMessage {
        val response = runApi {
            api.createIdeaThreadPoll(
                threadId,
                com.linkside.app.data.model.CreatePollRequest(question.trim(), options, allowMultiple),
            )
        }
        if (!response.ok || response.message == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to create poll")
        }
        return response.message
    }

    suspend fun votePoll(pollId: String, optionIds: List<String>): Poll {
        val response = runApi { api.votePoll(pollId, com.linkside.app.data.model.VotePollRequest(optionIds)) }
        if (!response.ok || response.poll == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to vote")
        }
        return response.poll
    }

    suspend fun closePoll(pollId: String): Poll {
        val response = runApi { api.closePoll(pollId) }
        if (!response.ok || response.poll == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to close poll")
        }
        return response.poll
    }

    suspend fun deletePoll(pollId: String) {
        val response = runApi { api.deletePoll(pollId) }
        if (!response.ok) {
            throw com.linkside.app.data.api.ApiException("Failed to delete poll")
        }
    }

    suspend fun fetchTeeTimePhotos(teeTimeId: String): List<Photo> {
        val response = runApi { api.fetchTeeTimePhotos(teeTimeId) }
        if (!response.ok) return emptyList()
        return response.photos
    }

    suspend fun uploadTeeTimePhoto(teeTimeId: String, imageBytes: ByteArray, mimeType: String): Photo {
        val ext = if (mimeType == "image/png") "png" else "jpg"
        val body = imageBytes.toRequestBody(mimeType.toMediaType())
        val part = MultipartBody.Part.createFormData("photo", "photo.$ext", body)
        val response = runApi { api.uploadTeeTimePhoto(teeTimeId, part) }
        if (!response.ok || response.photo == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to upload photo")
        }
        return response.photo
    }

    suspend fun fetchRoundScores(): Map<String, Int> {
        val response = runApi { api.fetchRoundScores() }
        if (!response.ok) return emptyMap()
        return response.scores.associate { it.teeTimeId to it.score }
    }

    suspend fun saveRoundScore(teeTimeId: String, score: Int) {
        runApi { api.saveRoundScore(teeTimeId, SaveRoundScoreRequest(score)) }
    }

    suspend fun deleteRoundScore(teeTimeId: String) {
        runApi { api.deleteRoundScore(teeTimeId) }
    }

    suspend fun fetchTeeTimeScorecards(teeTimeId: String): List<TeeTimeScorecard> {
        val response = runApi { api.fetchTeeTimeScorecards(teeTimeId) }
        if (!response.ok) return emptyList()
        return response.scorecards
    }

    suspend fun fetchRoundSummary(teeTimeId: String): RoundSummary {
        val response = runApi { api.fetchRoundSummary(teeTimeId) }
        if (!response.ok || response.blurb.isNullOrBlank()) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to load round summary")
        }
        return RoundSummary(
            blurb = response.blurb,
            courseName = response.courseName.orEmpty(),
            hasScores = response.hasScores,
        )
    }

    suspend fun fetchIdeaThreads(): List<IdeaThread> {
        val response = runApi { api.fetchIdeaThreads() }
        if (!response.ok) throw com.linkside.app.data.api.ApiException("Failed to fetch idea threads")
        return response.threads.sortedByDescending { it.updatedAt }
    }

    suspend fun createIdeaThread(name: String, inviteePhones: List<String>): IdeaThread {
        val response = runApi {
            api.createIdeaThread(
                CreateIdeaThreadRequest(
                    name = name.trim(),
                    inviteePhones = inviteePhones.map { PhoneUtils.normalizePhone(it) },
                ),
            )
        }
        if (!response.ok || response.thread == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to create thread")
        }
        return response.thread
    }

    suspend fun fetchIdeaMessages(threadId: String): List<IdeaMessage> {
        val response = runApi { api.fetchIdeaMessages(threadId) }
        if (!response.ok) return emptyList()
        return response.messages
    }

    suspend fun sendIdeaMessage(threadId: String, text: String): IdeaMessage {
        val response = runApi { api.sendIdeaMessage(threadId, SendIdeaMessageRequest(text.trim())) }
        if (!response.ok || response.message == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to send message")
        }
        return response.message
    }

    suspend fun leaveIdeaThread(threadId: String) {
        runApi { api.leaveIdeaThread(threadId) }
    }

    suspend fun deleteIdeaThread(threadId: String) {
        runApi { api.deleteIdeaThread(threadId) }
    }

    suspend fun fetchNotifications(): List<AppNotification> {
        val response = runApi { api.fetchNotifications() }
        if (!response.ok) throw com.linkside.app.data.api.ApiException("Failed to fetch notifications")
        return response.notifications
    }

    suspend fun markNotificationsRead(ids: List<String>? = null) {
        runApi { api.markNotificationsRead(MarkNotificationsReadRequest(ids)) }
    }

    suspend fun deleteNotification(id: String) {
        runApi { api.deleteNotification(id) }
    }

    suspend fun registerDeviceToken(token: String) {
        val response = runApi {
            api.registerDeviceToken(DeviceTokenRequest(token = token, platform = "android"))
        }
        if (!response.ok) {
            throw com.linkside.app.data.api.ApiException("Failed to register device token")
        }
    }

    suspend fun unregisterDeviceToken() {
        runApi { api.unregisterDeviceToken() }
    }

    suspend fun fetchTournaments(): List<Tournament> {
        val response = runApi { api.fetchTournaments() }
        if (!response.ok) throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to fetch tournaments")
        return response.tournaments
    }

    suspend fun fetchTournament(id: String): Pair<Tournament, List<TournamentParticipant>> {
        val response = runApi { api.fetchTournament(id) }
        if (!response.ok || response.tournament == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to fetch tournament")
        }
        return response.tournament to response.participants
    }

    suspend fun fetchTournamentProducts(id: String): List<TournamentProduct> {
        val response = runApi { api.fetchTournamentProducts(id) }
        if (!response.ok) return emptyList()
        return response.products
    }

    suspend fun registerForTournament(
        id: String,
        productIds: List<String> = emptyList(),
        groupName: String? = null,
        teamName: String? = null,
    ): TournamentParticipant {
        val response = runApi {
            api.registerForTournament(
                id,
                TournamentRegisterRequest(
                    productIds = productIds,
                    groupName = groupName,
                    teamName = teamName,
                ),
            )
        }
        if (!response.ok || response.participant == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to register")
        }
        return response.participant
    }

    suspend fun withdrawFromTournament(id: String, ref: String): TournamentParticipant {
        val response = runApi {
            api.updateTournamentParticipantStatus(
                id,
                ref,
                com.linkside.app.data.model.TournamentParticipantStatusRequest(status = "withdrawn"),
            )
        }
        if (!response.ok || response.participant == null) {
            throw com.linkside.app.data.api.ApiException(response.error ?: "Failed to withdraw")
        }
        return response.participant
    }

    suspend fun fetchReferralSummary(): ReferralSummary {
        val response = runApi { api.fetchReferralSummary() }
        if (!response.ok) {
            throw ApiException(response.error ?: "Failed to load referral summary")
        }
        return ReferralSummary(
            invitedCount = response.invitedCount,
            joinedCount = response.joinedCount,
        )
    }

    suspend fun fetchContestLeaderboard(month: String? = null): ContestLeaderboard {
        val response = runApi { api.fetchContestLeaderboard(month) }
        if (!response.ok || response.month.isNullOrBlank() || response.prize.isNullOrBlank()) {
            throw ApiException(response.error ?: "Failed to load contest leaderboard")
        }
        return ContestLeaderboard(
            month = response.month,
            prize = response.prize,
            daysLeft = response.daysLeft,
            minJoinsToWin = response.minJoinsToWin,
            leaderboard = response.leaderboard,
            myRank = response.myRank,
            myCount = response.myCount,
        )
    }

    suspend fun fetchMyContestClaim(): ContestWin? {
        val response = runApi { api.fetchMyContestClaim() }
        if (!response.ok) {
            throw ApiException(response.error ?: "Failed to load contest claim")
        }
        return response.win
    }

    suspend fun claimContestPrize(
        month: String,
        name: String,
        email: String?,
        address: String,
        city: String?,
        state: String?,
        zip: String?,
    ) {
        val response = runApi {
            api.claimContestPrize(
                ContestClaimRequest(
                    month = month,
                    name = name,
                    email = email,
                    address = address,
                    city = city,
                    state = state,
                    zip = zip,
                ),
            )
        }
        if (!response.ok) {
            throw ApiException(response.error ?: "Failed to submit claim")
        }
    }
}
