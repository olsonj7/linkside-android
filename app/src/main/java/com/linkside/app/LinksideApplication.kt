package com.linkside.app

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.linkside.app.data.api.ApiClient
import com.linkside.app.data.auth.TokenStore
import com.linkside.app.data.prefs.ProfilePreferences
import com.linkside.app.data.repository.AuthRepository
import com.linkside.app.data.repository.LinksideRepository
import com.linkside.app.push.PushNotificationHelper
import com.linkside.app.ui.components.coursePhotoOkHttpClient

class LinksideApplication : Application(), ImageLoaderFactory {
    lateinit var authRepository: AuthRepository
        private set
    lateinit var linksideRepository: LinksideRepository
        private set
    lateinit var profilePreferences: ProfilePreferences
        private set

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "API_BASE_URL=${BuildConfig.API_BASE_URL} DEBUG=${BuildConfig.DEBUG}")
        val tokenStore = TokenStore(this)
        val api = ApiClient.create(tokenStore)
        authRepository = AuthRepository(api, tokenStore)
        linksideRepository = LinksideRepository(api)
        profilePreferences = ProfilePreferences(this)
        PushNotificationHelper.ensureChannel(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient { coursePhotoOkHttpClient() }
            .crossfade(true)
            .build()

    companion object {
        private const val TAG = "LinksideApi"
    }
}
