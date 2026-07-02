package com.linkside.app.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.linkside.app.BuildConfig
import com.linkside.app.data.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GoogleSignInHelper {
    suspend fun signIn(context: Context): Result<String> = withContext(Dispatchers.Main) {
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleCredential.idToken)
            } else {
                Result.failure(ApiException("Unexpected Google credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException(e.message ?: "Google sign-in failed"))
        }
    }
}
