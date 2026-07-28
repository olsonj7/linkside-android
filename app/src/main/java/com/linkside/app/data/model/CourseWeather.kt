package com.linkside.app.data.model

/**
 * Port of iOS `CourseWeather` — scoring and labels match WeatherService.swift.
 */
data class CourseWeather(
    val temperatureF: Double,
    val windSpeedMph: Double,
    val precipProbability: Int,
    val weatherCode: Int,
    val cloudCover: Int,
    val precipMm: Double,
    val rainLast24hMm: Double,
    val rainLast72hMm: Double,
    val rainLast7dMm: Double,
) {
    val temperatureDelta: Int
        get() = when {
            temperatureF >= 110 -> -20
            temperatureF >= 100 -> -10
            temperatureF >= 90 -> -5
            temperatureF >= 80 -> 0
            temperatureF >= 70 -> 5
            temperatureF >= 60 -> 0
            temperatureF >= 50 -> -5
            temperatureF >= 40 -> -20
            temperatureF >= 30 -> -30
            else -> -50
        }

    val windDelta: Int
        get() = when {
            windSpeedMph < 5 -> 5
            windSpeedMph < 10 -> 0
            windSpeedMph < 15 -> -5
            windSpeedMph < 20 -> -10
            windSpeedMph < 25 -> -20
            else -> -30
        }

    val precipDelta: Int
        get() = when {
            precipProbability < 10 -> 5
            precipProbability < 20 -> 0
            precipProbability < 30 -> -5
            precipProbability < 40 -> -10
            precipProbability < 75 -> -15
            precipProbability < 100 -> -20
            else -> -25
        }

    val drySurfaceBonus: Int
        get() = if (rainLast7dMm < 5) 5 else 0

    val overallScore: Int
        get() = (BASELINE_SCORE + temperatureDelta + windDelta + precipDelta + drySurfaceBonus)
            .coerceIn(0, 100)

    val scoreLabel: String
        get() = when {
            overallScore >= 90 -> "Amazing"
            overallScore >= 80 -> "Excellent"
            overallScore >= 70 -> "Great"
            overallScore >= 60 -> "Decent"
            overallScore >= 50 -> "Playable"
            overallScore >= 40 -> "Roughing it"
            overallScore >= 30 -> "Ouch"
            else -> "Not playable"
        }

    val scoreColorHex: String
        get() = when {
            overallScore >= 80 -> "34D399"
            overallScore >= 60 -> "6EE7A8"
            overallScore >= 40 -> "F59E0B"
            else -> "FF5A5F"
        }

    val conditionTags: List<String>
        get() {
            val tags = mutableListOf<String>()
            tags += when {
                temperatureF < 32 -> "Freezing"
                temperatureF < 45 -> "Very cold"
                temperatureF < 55 -> "Cold"
                temperatureF < 65 -> "Cool temps"
                temperatureF < 75 -> "Mild temps"
                temperatureF < 85 -> "Warm & pleasant"
                temperatureF < 95 -> "Hot"
                else -> "Extreme heat"
            }
            when {
                windSpeedMph < 8 -> Unit
                windSpeedMph < 15 -> tags += "Light breeze"
                windSpeedMph < 25 -> tags += "Moderate wind"
                else -> tags += "Very windy"
            }
            when {
                weatherCode in 95..99 -> tags += "Storm risk"
                weatherCode in 71..86 -> tags += "Snow/ice"
                precipProbability >= 60 || weatherCode in 51..82 -> tags += "Expect rain"
                precipProbability >= 30 -> tags += "Chance of rain"
                rainLast72hMm > 10 -> tags += "Wet from recent rain"
                rainLast72hMm > 3 -> tags += "Slightly soft"
                rainLast72hMm < 1 && temperatureF >= 60 -> tags += "Dry & firm"
            }
            return tags.take(3)
        }

    val conditionLabel: String
        get() = when (weatherCode) {
            0 -> "Clear"
            1 -> "Mostly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            in 51..55 -> "Drizzle"
            in 56..57 -> "Freezing Drizzle"
            in 61..65 -> "Rain"
            in 66..67 -> "Freezing Rain"
            in 71..75 -> "Snow"
            77 -> "Snow Grains"
            in 80..82 -> "Heavy Rain"
            in 85..86 -> "Snow Showers"
            95 -> "Thunderstorm"
            in 96..99 -> "Severe Storm"
            else -> "Cloudy"
        }

    companion object {
        const val BASELINE_SCORE = 80
    }
}

data class CourseLocationResponse(
    val ok: Boolean = false,
    val lat: Double? = null,
    val lng: Double? = null,
    val error: String? = null,
)

data class WeatherFunSummaryRequest(
    val courseName: String,
    val teeTimeDate: Long,
    val temperatureF: Double,
    val windSpeedMph: Double,
    val precipProbability: Int,
    val weatherCode: Int,
    val cloudCover: Int,
    val score: Int,
)

data class WeatherFunSummaryResponse(
    val ok: Boolean = false,
    val summary: String? = null,
    val error: String? = null,
)

fun TeeTime.isWithinCourseConditionsWindow(now: java.time.Instant = java.time.Instant.now()): Boolean {
    val date = parsedInstant() ?: return false
    if (!date.isAfter(now)) return false
    val sevenDays = now.plusSeconds(7L * 24 * 3600)
    return date.isBefore(sevenDays)
}
