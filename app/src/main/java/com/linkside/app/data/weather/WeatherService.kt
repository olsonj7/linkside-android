package com.linkside.app.data.weather

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.linkside.app.data.model.CourseWeather
import com.linkside.app.data.model.WeatherFunSummaryRequest
import com.linkside.app.data.repository.LinksideRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Port of iOS `WeatherService` — Open-Meteo multi-model blend + backend course location.
 */
class WeatherService(
    private val repository: LinksideRepository,
    private val appContext: Context,
    private val httpClient: OkHttpClient = defaultClient,
) {
    suspend fun fetchWeatherForCourse(
        placeId: String?,
        name: String,
        forDate: Instant,
    ): CourseWeather {
        val coords = repository.courseLocation(placeId, name)
            ?: geocodeCourse(name)
            ?: throw IllegalStateException("Could not locate course")
        return fetchWeather(coords.first, coords.second, forDate)
    }

    suspend fun weatherFunSummary(
        courseName: String,
        teeTimeDate: Instant,
        weather: CourseWeather,
    ): String? = repository.weatherFunSummary(
        WeatherFunSummaryRequest(
            courseName = courseName,
            teeTimeDate = teeTimeDate.toEpochMilli(),
            temperatureF = weather.temperatureF,
            windSpeedMph = weather.windSpeedMph,
            precipProbability = weather.precipProbability,
            weatherCode = weather.weatherCode,
            cloudCover = weather.cloudCover,
            score = weather.overallScore,
        ),
    )

    suspend fun fetchWeather(lat: Double, lng: Double, forDate: Instant): CourseWeather =
        withContext(Dispatchers.IO) {
            val models = listOf("ecmwf_ifs025", "icon_seamless", "gfs_seamless", "gem_seamless")
            val url = "https://api.open-meteo.com/v1/forecast".toHttpUrl().newBuilder()
                .addQueryParameter("latitude", lat.toString())
                .addQueryParameter("longitude", lng.toString())
                .addQueryParameter(
                    "hourly",
                    "temperature_2m,wind_speed_10m,precipitation_probability,weather_code,cloud_cover,precipitation",
                )
                .addQueryParameter("temperature_unit", "fahrenheit")
                .addQueryParameter("wind_speed_unit", "mph")
                .addQueryParameter("timezone", "UTC")
                .addQueryParameter("past_days", "7")
                .addQueryParameter("forecast_days", "16")
                .addQueryParameter("models", models.joinToString(","))
                .build()

            val request = Request.Builder().url(url).get().build()
            val body = httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("Open-Meteo HTTP ${response.code}")
                response.body?.string() ?: throw IllegalStateException("Empty Open-Meteo body")
            }

            val root = JSONObject(body)
            val hourly = root.getJSONObject("hourly")
            val times = hourly.getJSONArray("time").toStringList()

            fun blendedMean(variable: String): List<Double?> {
                val count = times.size
                val sums = DoubleArray(count)
                val counts = IntArray(count)
                for (model in models) {
                    val key = "${variable}_$model"
                    if (!hourly.has(key)) continue
                    val arr = hourly.getJSONArray(key)
                    for (i in 0 until minOf(count, arr.length())) {
                        if (!arr.isNull(i)) {
                            sums[i] += arr.getDouble(i)
                            counts[i] += 1
                        }
                    }
                }
                return List(count) { i -> if (counts[i] > 0) sums[i] / counts[i] else null }
            }

            fun firstAvailable(variable: String): List<Double?> {
                val count = times.size
                val result = arrayOfNulls<Double>(count)
                for (model in models) {
                    val key = "${variable}_$model"
                    if (!hourly.has(key)) continue
                    val arr = hourly.getJSONArray(key)
                    for (i in 0 until minOf(count, arr.length())) {
                        if (result[i] == null && !arr.isNull(i)) {
                            result[i] = arr.getDouble(i)
                        }
                    }
                }
                return result.toList()
            }

            val tempArr = blendedMean("temperature_2m")
            val windArr = blendedMean("wind_speed_10m")
            val precipProbArr = blendedMean("precipitation_probability")
            val cloudArr = blendedMean("cloud_cover")
            val precipArr = blendedMean("precipitation")
            val codeArr = firstAvailable("weather_code")

            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            val target = forDate.epochSecond.toDouble()
            var bestIdx = 0
            var bestDiff = Double.MAX_VALUE
            times.forEachIndexed { i, timeStr ->
                val t = runCatching {
                    LocalDateTime.parse(timeStr, fmt).toEpochSecond(ZoneOffset.UTC).toDouble()
                }.getOrNull() ?: return@forEachIndexed
                val diff = abs(t - target)
                if (diff < bestDiff) {
                    bestDiff = diff
                    bestIdx = i
                }
            }

            var resolvedIdx = bestIdx
            for (offset in times.indices) {
                val candidate = bestIdx - offset
                if (candidate >= 0 && tempArr[candidate] != null) {
                    resolvedIdx = candidate
                    break
                }
            }

            val temp = tempArr[resolvedIdx] ?: throw IllegalStateException("No temperature data")
            val precipAtTee = precipArr[resolvedIdx] ?: 0.0
            val idx24hAgo = (resolvedIdx - 24).coerceAtLeast(0)
            val idx72hAgo = (resolvedIdx - 72).coerceAtLeast(0)
            val idx7dAgo = (resolvedIdx - 168).coerceAtLeast(0)

            fun sumPrecip(from: Int, to: Int): Double {
                var sum = 0.0
                for (i in from until to) {
                    sum += precipArr.getOrNull(i) ?: 0.0
                }
                return sum
            }

            CourseWeather(
                temperatureF = temp,
                windSpeedMph = windArr[resolvedIdx] ?: 0.0,
                precipProbability = (precipProbArr[resolvedIdx] ?: 0.0).roundToInt(),
                weatherCode = (codeArr[resolvedIdx] ?: 0.0).roundToInt(),
                cloudCover = (cloudArr[resolvedIdx] ?: 0.0).roundToInt(),
                precipMm = precipAtTee,
                rainLast24hMm = sumPrecip(idx24hAgo, resolvedIdx),
                rainLast72hMm = sumPrecip(idx72hAgo, resolvedIdx),
                rainLast7dMm = sumPrecip(idx7dAgo, resolvedIdx),
            )
        }

    @Suppress("DEPRECATION")
    private suspend fun geocodeCourse(name: String): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(appContext, Locale.getDefault())
            val query = "$name golf course"
            try {
                if (Build.VERSION.SDK_INT >= 33) {
                    suspendCancellableCoroutine<Pair<Double, Double>?> { cont ->
                        geocoder.getFromLocationName(query, 1) { list ->
                            val first = list.firstOrNull()
                            cont.resume(
                                if (first != null) first.latitude to first.longitude else null,
                            )
                        }
                    }
                } else {
                    val list = geocoder.getFromLocationName(query, 1)
                    val first = list?.firstOrNull() ?: return@withContext null
                    first.latitude to first.longitude
                }
            } catch (_: Exception) {
                null
            }
        }

    companion object {
        private val defaultClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}

private fun JSONArray.toStringList(): List<String> =
    List(length()) { i -> getString(i) }
