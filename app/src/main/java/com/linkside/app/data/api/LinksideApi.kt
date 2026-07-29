package com.linkside.app.data.api

import com.linkside.app.BuildConfig
import com.linkside.app.data.auth.TokenStore
import com.linkside.app.data.model.AddInvitesRequest
import com.linkside.app.data.model.AddInvitesResponse
import com.linkside.app.data.model.AuthResponse
import com.linkside.app.data.model.BumpInviteeRequest
import com.linkside.app.data.model.ToggleInviteAccessRequest
import com.linkside.app.data.model.ContactStatusRequest
import com.linkside.app.data.model.ContactStatusResponse
import com.linkside.app.data.model.ContestClaimRequest
import com.linkside.app.data.model.ContestClaimResponse
import com.linkside.app.data.model.ContestClaimSubmitResponse
import com.linkside.app.data.model.ContestLeaderboardResponse
import com.linkside.app.data.model.ManualInvite
import com.linkside.app.data.model.OptInMessageRequest
import com.linkside.app.data.model.CourseLocationResponse
import com.linkside.app.data.model.CourseSearchResponse
import com.linkside.app.data.model.CourseWebsiteResponse
import com.linkside.app.data.model.CreateFriendGroupRequest
import com.linkside.app.data.model.CreateIdeaThreadRequest
import com.linkside.app.data.model.CreateTeeTimeRequest
import com.linkside.app.data.model.DeleteTeeTimeResponse
import com.linkside.app.data.model.DeviceTokenRequest
import com.linkside.app.data.model.UpdateTeeTimeRequest
import com.linkside.app.data.model.FriendGroupResponse
import com.linkside.app.data.model.ForgotPasswordRequest
import com.linkside.app.data.model.GolfTripsListResponse
import com.linkside.app.data.model.GolfTripResponse
import com.linkside.app.data.model.GolfersResponse
import com.linkside.app.data.model.EmailLoginRequest
import com.linkside.app.data.model.EmailRegisterRequest
import com.linkside.app.data.model.GoogleAuthRequest
import com.linkside.app.data.model.IdeaMessageResponse
import com.linkside.app.data.model.IdeaMessagesResponse
import com.linkside.app.data.model.IdeaThreadResponse
import com.linkside.app.data.model.IdeaThreadsResponse
import com.linkside.app.data.model.LinkEmailRequest
import com.linkside.app.data.model.LinkPhoneRequest
import com.linkside.app.data.model.MarkNotificationsReadRequest
import com.linkside.app.data.model.NotificationsResponse
import com.linkside.app.data.model.OkResponse
import com.linkside.app.data.model.PhotoResponse
import com.linkside.app.data.model.PhotosResponse
import com.linkside.app.data.model.PostAnnouncementRequest
import com.linkside.app.data.model.PostAnnouncementResponse
import com.linkside.app.data.model.ReferralSummaryResponse
import com.linkside.app.data.model.TripAnnouncementsResponse
import com.linkside.app.data.model.RemoveInviteRequest
import com.linkside.app.data.model.ResetPasswordRequest
import com.linkside.app.data.model.RoundScoresResponse
import com.linkside.app.data.model.RoundSummaryResponse
import com.linkside.app.data.model.SaveGolfersRequest
import com.linkside.app.data.model.CreatePollRequest
import com.linkside.app.data.model.MessageReactionRequest
import com.linkside.app.data.model.PollResponse
import com.linkside.app.data.model.SaveRoundScoreRequest
import com.linkside.app.data.model.SendInvitesResponse
import com.linkside.app.data.model.VotePollRequest
import com.linkside.app.data.model.SendCodeRequest
import com.linkside.app.data.model.SendIdeaMessageRequest
import com.linkside.app.data.model.SendTripMessageRequest
import com.linkside.app.data.model.SendTeeTimeMessageRequest
import com.linkside.app.data.model.TeeTimeListResponse
import com.linkside.app.data.model.TeeTimeMessageResponse
import com.linkside.app.data.model.TeeTimeMessagesResponse
import com.linkside.app.data.model.TeeTimeResponse
import com.linkside.app.data.model.TeeTimeScorecardsResponse
import com.linkside.app.data.model.TournamentDetailResponse
import com.linkside.app.data.model.TournamentParticipantResponse
import com.linkside.app.data.model.TournamentParticipantStatusRequest
import com.linkside.app.data.model.TournamentProductsResponse
import com.linkside.app.data.model.TournamentRegisterRequest
import com.linkside.app.data.model.TournamentRegisterResponse
import com.linkside.app.data.model.TournamentsListResponse
import com.linkside.app.data.model.TripMessageResponse
import com.linkside.app.data.model.TripMessagesResponse
import com.linkside.app.data.model.TripPaymentRequest
import com.linkside.app.data.model.TripRsvpRequest
import com.linkside.app.data.model.UpdateFriendGroupRequest
import com.linkside.app.data.model.UpdateInviteStatusRequest
import com.linkside.app.data.model.UpdateProfileRequest
import com.linkside.app.data.model.UserResponse
import com.linkside.app.data.model.VerifyCodeRequest
import com.linkside.app.data.model.WeatherFunSummaryRequest
import com.linkside.app.data.model.WeatherFunSummaryResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface LinksideApi {
    // Auth
    @POST("send-code")
    suspend fun sendCode(@Body body: SendCodeRequest): OkResponse

    @POST("verify-code")
    suspend fun verifyCode(@Body body: VerifyCodeRequest): OkResponse

    @POST("auth/google")
    suspend fun googleAuth(@Body body: GoogleAuthRequest): AuthResponse

    @POST("auth/email/login")
    suspend fun emailLogin(@Body body: EmailLoginRequest): AuthResponse

    @POST("auth/email/register")
    suspend fun emailRegister(@Body body: EmailRegisterRequest): AuthResponse

    @POST("auth/email/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): OkResponse

    @POST("auth/email/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): OkResponse

    @GET("me")
    suspend fun me(): UserResponse

    @PATCH("me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserResponse

    @PATCH("me")
    suspend fun patchProfile(@Body body: Map<String, @JvmSuppressWildcards Any?>): UserResponse

    @DELETE("me")
    suspend fun deleteAccount(): OkResponse

    @POST("me/link-phone")
    suspend fun linkPhone(@Body body: LinkPhoneRequest): UserResponse

    @POST("me/link-email")
    suspend fun linkEmail(@Body body: LinkEmailRequest): UserResponse

    @POST("me/link-google")
    suspend fun linkGoogle(@Body body: GoogleAuthRequest): UserResponse

    @Multipart
    @POST("me/avatar")
    suspend fun uploadAvatar(@Part photo: MultipartBody.Part): UserResponse

    @DELETE("me/avatar")
    suspend fun deleteAvatar(): UserResponse

    // Golfers
    @GET("me/golfers")
    suspend fun fetchSavedGolfers(): GolfersResponse

    @PUT("me/golfers")
    suspend fun saveGolfers(@Body body: SaveGolfersRequest): GolfersResponse

    // Friend groups
    @GET("friend-groups")
    suspend fun fetchFriendGroups(): FriendGroupResponse

    @POST("friend-groups")
    suspend fun createFriendGroup(@Body body: CreateFriendGroupRequest): FriendGroupResponse

    @PUT("friend-groups/{id}")
    suspend fun updateFriendGroup(
        @Path(value = "id", encoded = true) id: String,
        @Body body: UpdateFriendGroupRequest,
    ): FriendGroupResponse

    @DELETE("friend-groups/{id}")
    suspend fun deleteFriendGroup(@Path(value = "id", encoded = true) id: String): OkResponse

    // Contacts
    @POST("contacts/status")
    suspend fun checkContactStatuses(@Body body: ContactStatusRequest): ContactStatusResponse

    @POST("contacts/opt-in-message")
    suspend fun getOptInMessage(@Body body: OptInMessageRequest): ManualInvite

    // Tee times
    @GET("tee-times")
    suspend fun fetchTeeTimes(): TeeTimeListResponse

    @GET("tee-times/{id}")
    suspend fun fetchTeeTime(@Path("id") id: String): TeeTimeResponse

    @POST("tee-times")
    suspend fun createTeeTime(@Body body: CreateTeeTimeRequest): TeeTimeResponse

    @PATCH("tee-times/{id}")
    suspend fun updateTeeTime(
        @Path("id") id: String,
        @Body body: UpdateTeeTimeRequest,
    ): TeeTimeResponse

    @DELETE("tee-times/{id}")
    suspend fun deleteTeeTime(@Path("id") id: String): DeleteTeeTimeResponse

    @POST("tee-times/{id}/invites")
    suspend fun addTeeTimeInvites(
        @Path("id") id: String,
        @Body body: AddInvitesRequest,
    ): AddInvitesResponse

    /** Send invites for golfers saved via "Save without inviting". */
    @POST("tee-times/{id}/send-invites")
    suspend fun sendPendingInvites(@Path("id") id: String): SendInvitesResponse

    @POST("tee-times/{id}/invites/remove")
    suspend fun removeTeeTimeInvite(
        @Path("id") id: String,
        @Body body: RemoveInviteRequest,
    ): TeeTimeResponse

    @POST("tee-times/{id}/bump")
    suspend fun bumpInvitee(
        @Path("id") id: String,
        @Body body: BumpInviteeRequest,
    ): OkResponse

    @POST("tee-times/{id}/invites/toggle-invite-access")
    suspend fun toggleInviteAccess(
        @Path("id") id: String,
        @Body body: ToggleInviteAccessRequest,
    ): TeeTimeResponse

    @POST("tee-times/{id}/update-status")
    suspend fun updateInviteStatus(
        @Path("id") id: String,
        @Body body: UpdateInviteStatusRequest,
    ): TeeTimeResponse

    @GET("tee-times/{id}/messages")
    suspend fun fetchTeeTimeMessages(@Path("id") id: String): TeeTimeMessagesResponse

    @POST("tee-times/{id}/messages")
    suspend fun sendTeeTimeMessage(
        @Path("id") id: String,
        @Body body: SendTeeTimeMessageRequest,
    ): TeeTimeMessageResponse

    @POST("tee-times/{id}/messages/{messageId}/reactions")
    suspend fun toggleTeeTimeReaction(
        @Path("id") id: String,
        @Path("messageId") messageId: String,
        @Body body: MessageReactionRequest,
    ): TeeTimeMessageResponse

    @GET("tee-times/{id}/photos")
    suspend fun fetchTeeTimePhotos(@Path("id") id: String): PhotosResponse

    @Multipart
    @POST("tee-times/{id}/photos")
    suspend fun uploadTeeTimePhoto(@Path("id") id: String, @Part photo: MultipartBody.Part): PhotoResponse

    @DELETE("tee-times/{id}/photos/{photoId}")
    suspend fun deleteTeeTimePhoto(@Path("id") id: String, @Path("photoId") photoId: String): OkResponse

    @POST("tee-times/{id}/score")
    suspend fun saveRoundScore(@Path("id") id: String, @Body body: SaveRoundScoreRequest): OkResponse

    @DELETE("tee-times/{id}/score")
    suspend fun deleteRoundScore(@Path("id") id: String): OkResponse

    @GET("round-scores")
    suspend fun fetchRoundScores(): RoundScoresResponse

    @GET("tee-times/{id}/scorecards")
    suspend fun fetchTeeTimeScorecards(@Path("id") id: String): TeeTimeScorecardsResponse

    /** Shareable AI blurb for a completed round (free for all tiers). */
    @GET("tee-times/{id}/round-summary")
    suspend fun fetchRoundSummary(@Path("id") id: String): RoundSummaryResponse

    // Courses
    @GET("courses/search")
    suspend fun searchCourses(
        @Query("q") query: String,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
    ): CourseSearchResponse

    @GET("courses/location")
    suspend fun courseLocation(
        @Query("placeId") placeId: String? = null,
        @Query("name") name: String? = null,
    ): CourseLocationResponse

    @GET("courses/website")
    suspend fun courseWebsite(@Query("placeId") placeId: String): CourseWebsiteResponse

    @POST("weather/fun-summary")
    suspend fun weatherFunSummary(@Body body: WeatherFunSummaryRequest): WeatherFunSummaryResponse

    // Golf trips (invitee flows — no create/update/delete)
    @GET("golf-trips")
    suspend fun fetchGolfTrips(): GolfTripsListResponse

    @GET("golf-trips/{id}")
    suspend fun fetchGolfTrip(@Path("id") id: String): GolfTripResponse

    @POST("golf-trips/{id}/rsvp")
    suspend fun rsvpGolfTrip(@Path("id") id: String, @Body body: TripRsvpRequest): GolfTripResponse

    @PATCH("golf-trips/{id}/invites/deposit-paid")
    suspend fun setTripDepositPaid(@Path("id") id: String, @Body body: TripPaymentRequest): GolfTripResponse

    @PATCH("golf-trips/{id}/invites/balance-paid")
    suspend fun setTripBalancePaid(@Path("id") id: String, @Body body: TripPaymentRequest): GolfTripResponse

    @GET("golf-trips/{id}/tee-times")
    suspend fun fetchTripTeeTimes(@Path("id") id: String): TeeTimeListResponse

    @GET("golf-trips/{id}/announcements")
    suspend fun fetchTripAnnouncements(@Path("id") id: String): TripAnnouncementsResponse

    @POST("golf-trips/{id}/announcements")
    suspend fun postTripAnnouncement(
        @Path("id") id: String,
        @Body body: PostAnnouncementRequest,
    ): PostAnnouncementResponse

    @GET("golf-trips/{id}/messages")
    suspend fun fetchTripMessages(@Path("id") id: String): TripMessagesResponse

    @POST("golf-trips/{id}/messages")
    suspend fun sendTripMessage(@Path("id") id: String, @Body body: SendTripMessageRequest): TripMessageResponse

    @POST("golf-trips/{id}/messages/{messageId}/reactions")
    suspend fun toggleTripReaction(
        @Path("id") id: String,
        @Path("messageId") messageId: String,
        @Body body: MessageReactionRequest,
    ): TripMessageResponse

    // POLLS (idea threads + golf-trip chats)

    @POST("idea-threads/{id}/polls")
    suspend fun createIdeaThreadPoll(
        @Path("id") id: String,
        @Body body: CreatePollRequest,
    ): IdeaMessageResponse

    @POST("golf-trips/{id}/polls")
    suspend fun createTripPoll(
        @Path("id") id: String,
        @Body body: CreatePollRequest,
    ): TripMessageResponse

    @POST("polls/{pollId}/vote")
    suspend fun votePoll(
        @Path("pollId") pollId: String,
        @Body body: VotePollRequest,
    ): PollResponse

    @POST("polls/{pollId}/close")
    suspend fun closePoll(@Path("pollId") pollId: String): PollResponse

    @DELETE("polls/{pollId}")
    suspend fun deletePoll(@Path("pollId") pollId: String): OkResponse

    @GET("golf-trips/{id}/photos")
    suspend fun fetchTripPhotos(@Path("id") id: String): PhotosResponse

    @Multipart
    @POST("golf-trips/{id}/photos")
    suspend fun uploadTripPhoto(@Path("id") id: String, @Part photo: MultipartBody.Part): PhotoResponse

    // Idea threads
    @GET("idea-threads")
    suspend fun fetchIdeaThreads(): IdeaThreadsResponse

    @POST("idea-threads")
    suspend fun createIdeaThread(@Body body: CreateIdeaThreadRequest): IdeaThreadResponse

    @GET("idea-threads/{id}/messages")
    suspend fun fetchIdeaMessages(@Path("id") id: String): IdeaMessagesResponse

    @POST("idea-threads/{id}/messages")
    suspend fun sendIdeaMessage(
        @Path("id") id: String,
        @Body body: SendIdeaMessageRequest,
    ): IdeaMessageResponse

    @POST("idea-threads/{id}/leave")
    suspend fun leaveIdeaThread(@Path("id") id: String): OkResponse

    @DELETE("idea-threads/{id}")
    suspend fun deleteIdeaThread(@Path("id") id: String): OkResponse

    // Notifications
    @GET("notifications")
    suspend fun fetchNotifications(): NotificationsResponse

    @POST("notifications/mark-read")
    suspend fun markNotificationsRead(@Body body: MarkNotificationsReadRequest): OkResponse

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): OkResponse

    @POST("device-token")
    suspend fun registerDeviceToken(@Body body: DeviceTokenRequest): OkResponse

    @DELETE("device-token")
    suspend fun unregisterDeviceToken(): OkResponse

    // Tournaments (Bronze: list, detail, register)
    @GET("tournaments")
    suspend fun fetchTournaments(): TournamentsListResponse

    @GET("tournaments/{id}")
    suspend fun fetchTournament(@Path("id") id: String): TournamentDetailResponse

    @GET("tournaments/{id}/products")
    suspend fun fetchTournamentProducts(@Path("id") id: String): TournamentProductsResponse

    @POST("tournaments/{id}/register")
    suspend fun registerForTournament(
        @Path("id") id: String,
        @Body body: TournamentRegisterRequest,
    ): TournamentRegisterResponse

    // Self RSVP update (e.g. withdraw). Backend matches by the authenticated user,
    // so the path segment is only a placeholder.
    @PATCH("tournaments/{id}/participants/{ref}")
    suspend fun updateTournamentParticipantStatus(
        @Path("id") id: String,
        @Path("ref") ref: String,
        @Body body: TournamentParticipantStatusRequest,
    ): TournamentParticipantResponse

    // Invite Contest (monthly prize for top referrers by friends who joined)
    @GET("referrals/summary")
    suspend fun fetchReferralSummary(): ReferralSummaryResponse

    @GET("referrals/leaderboard")
    suspend fun fetchContestLeaderboard(@Query("month") month: String? = null): ContestLeaderboardResponse

    @GET("contest/my-claim")
    suspend fun fetchMyContestClaim(): ContestClaimResponse

    @POST("contest/claim")
    suspend fun claimContestPrize(@Body body: ContestClaimRequest): ContestClaimSubmitResponse
}

object ApiClient {
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun create(tokenStore: TokenStore): LinksideApi {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
            val token = tokenStore.readToken()
            val authenticated = if (!token.isNullOrBlank()) {
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                request
            }
            chain.proceed(authenticated)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val baseUrl = ensureTrailingSlash(BuildConfig.API_BASE_URL)
        if (BuildConfig.DEBUG) {
            android.util.Log.i("LinksideApi", "Retrofit baseUrl=$baseUrl")
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi).withNullSerialization())
            .build()
            .create(LinksideApi::class.java)
    }

    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}

suspend fun <T> runApi(block: suspend () -> T): T {
    return try {
        block()
    } catch (e: HttpException) {
        throw ApiException(parseApiError(e.response()?.errorBody()?.string()))
    }
}
