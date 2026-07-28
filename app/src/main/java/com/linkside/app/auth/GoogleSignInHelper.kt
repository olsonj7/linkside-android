package com.linkside.app.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.linkside.app.BuildConfig
import com.linkside.app.data.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GoogleSignInHelper {
    /**
     * Explicit "Continue with Google" button flow.
     * Uses [GetSignInWithGoogleOption] (full account picker), not One Tap —
     * One Tap often returns "No credentials available" for first-time sign-in.
     */
    suspend fun signIn(context: Context): Result<String> = withContext(Dispatchers.Main) {
        try {
            val credentialManager = CredentialManager.create(context)
            val signInOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
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
        } catch (e: NoCredentialException) {
            Result.failure(
                ApiException(
                    "No Google accounts available. Sign into a Google account on this device, " +
                        "and confirm the Android OAuth client SHA-1 is registered in Google Cloud.",
                ),
            )
        } catch (e: Exception) {
            Result.failure(ApiException(e.message ?: "Google sign-in failed"))
        }
    }
}
