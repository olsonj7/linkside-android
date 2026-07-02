package com.linkside.app.data.api

import com.linkside.app.BuildConfig
import com.linkside.app.data.auth.TokenStore
import com.linkside.app.data.model.AuthResponse
import com.linkside.app.data.model.ContactStatusRequest
import com.linkside.app.data.model.ContactStatusResponse
import com.linkside.app.data.model.CourseSearchResponse
import com.linkside.app.data.model.CreateFriendGroupRequest
import com.linkside.app.data.model.CreateTeeTimeRequest
import com.linkside.app.data.model.FriendGroupResponse
import com.linkside.app.data.model.GolfTripsListResponse
import com.linkside.app.data.model.GolfTripResponse
import com.linkside.app.data.model.GolfersResponse
import com.linkside.app.data.model.EmailLoginRequest
import com.linkside.app.data.model.EmailRegisterRequest
import com.linkside.app.data.model.GoogleAuthRequest
import com.linkside.app.data.model.OkResponse
import com.linkside.app.data.model.PhotoResponse
import com.linkside.app.data.model.PhotosResponse
import com.linkside.app.data.model.SaveGolfersRequest
import com.linkside.app.data.model.SendCodeRequest
import com.linkside.app.data.model.SendTripMessageRequest
import com.linkside.app.data.model.TeeTimeListResponse
import com.linkside.app.data.model.TeeTimeResponse
import com.linkside.app.data.model.TripMessageResponse
import com.linkside.app.data.model.TripMessagesResponse
import com.linkside.app.data.model.TripPaymentRequest
import com.linkside.app.data.model.TripRsvpRequest
import com.linkside.app.data.model.UpdateFriendGroupRequest
import com.linkside.app.data.model.UpdateInviteStatusRequest
import com.linkside.app.data.model.UpdateProfileRequest
import com.linkside.app.data.model.UserResponse
import com.linkside.app.data.model.VerifyCodeRequest
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

    @GET("me")
    suspend fun me(): UserResponse

    @PATCH("me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserResponse

    @PATCH("me")
    suspend fun patchProfile(@Body body: Map<String, @JvmSuppressWildcards Any?>): UserResponse

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
    suspend fun updateFriendGroup(@Path("id") id: String, @Body body: UpdateFriendGroupRequest): FriendGroupResponse

    @DELETE("friend-groups/{id}")
    suspend fun deleteFriendGroup(@Path("id") id: String): OkResponse

    // Contacts
    @POST("contacts/status")
    suspend fun checkContactStatuses(@Body body: ContactStatusRequest): ContactStatusResponse

    // Tee times
    @GET("tee-times")
    suspend fun fetchTeeTimes(): TeeTimeListResponse

    @GET("tee-times/{id}")
    suspend fun fetchTeeTime(@Path("id") id: String): TeeTimeResponse

    @POST("tee-times")
    suspend fun createTeeTime(@Body body: CreateTeeTimeRequest): TeeTimeResponse

    @POST("tee-times/{id}/update-status")
    suspend fun updateInviteStatus(
        @Path("id") id: String,
        @Body body: UpdateInviteStatusRequest,
    ): TeeTimeResponse

    // Courses
    @GET("courses/search")
    suspend fun searchCourses(@Query("q") query: String): CourseSearchResponse

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

    @GET("golf-trips/{id}/messages")
    suspend fun fetchTripMessages(@Path("id") id: String): TripMessagesResponse

    @POST("golf-trips/{id}/messages")
    suspend fun sendTripMessage(@Path("id") id: String, @Body body: SendTripMessageRequest): TripMessageResponse

    @GET("golf-trips/{id}/photos")
    suspend fun fetchTripPhotos(@Path("id") id: String): PhotosResponse

    @Multipart
    @POST("golf-trips/{id}/photos")
    suspend fun uploadTripPhoto(@Path("id") id: String, @Part photo: MultipartBody.Part): PhotoResponse
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

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(BuildConfig.API_BASE_URL))
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
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
