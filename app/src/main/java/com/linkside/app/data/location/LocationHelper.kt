package com.linkside.app.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * One-shot coarse-location lookup used to bias golf course search toward nearby
 * courses (mirrors iOS, which passes the device coordinate to `/courses/search`).
 * Every call is best-effort: any failure or missing permission returns null so
 * search still works without location.
 */
object LocationHelper {

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    suspend fun currentLatLng(context: Context): Pair<Double, Double>? {
        if (!hasPermission(context)) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        return try {
            // Prefer the cached fix (instant); fall back to an active request.
            val cached = client.lastLocation.await()
            if (cached != null) {
                cached.latitude to cached.longitude
            } else {
                val cts = CancellationTokenSource()
                val fresh = client
                    .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .await()
                fresh?.let { it.latitude to it.longitude }
            }
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }
}
