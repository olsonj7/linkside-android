package com.linkside.app

import android.app.Application
import com.linkside.app.data.api.ApiClient
import com.linkside.app.data.auth.TokenStore
import com.linkside.app.data.prefs.ProfilePreferences
import com.linkside.app.data.repository.AuthRepository
import com.linkside.app.data.repository.LinksideRepository

class LinksideApplication : Application() {
    lateinit var authRepository: AuthRepository
        private set
    lateinit var linksideRepository: LinksideRepository
        private set
    lateinit var profilePreferences: ProfilePreferences
        private set

    override fun onCreate() {
        super.onCreate()
        val tokenStore = TokenStore(this)
        val api = ApiClient.create(tokenStore)
        authRepository = AuthRepository(api, tokenStore)
        linksideRepository = LinksideRepository(api)
        profilePreferences = ProfilePreferences(this)
    }
}
