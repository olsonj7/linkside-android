package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.linkside.app.LinksideApplication
import com.linkside.app.data.model.CourseWeather
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.isWithinCourseConditionsWindow
import com.linkside.app.data.weather.WeatherService
import com.linkside.app.ui.theme.LinksideColors
import java.time.Instant
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseConditionsCard(
    teeTime: TeeTime,
    isSilver: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!teeTime.isWithinCourseConditionsWindow()) return

    val context = LocalContext.current
    val weatherService = remember {
        val app = context.applicationContext as LinksideApplication
        WeatherService(app.linksideRepository, app)
    }

    var weather by remember(teeTime.id) { mutableStateOf<CourseWeather?>(null) }
    var aiSummary by remember(teeTime.id) { mutableStateOf<String?>(null) }
    var isLoading by remember(teeTime.id) { mutableStateOf(true) }
    var isLoadingSummary by remember(teeTime.id) { mutableStateOf(false) }
    var failed by remember(teeTime.id) { mutableStateOf(false) }
    var lastFetchedAt by remember(teeTime.id) { mutableLongStateOf(0L) }
    var showBreakdown by remember { mutableStateOf(false) }

    fun isCacheStale(): Boolean {
        if (lastFetchedAt == 0L) return true
        return Instant.now().epochSecond - lastFetchedAt > 3600
    }

    LaunchedEffect(teeTime.id, isSilver) {
        if (!isCacheStale() && weather != null) return@LaunchedEffect
        isLoading = weather == null
        failed = false
        isLoadingSummary = isSilver
        if (!isSilver) {
            aiSummary = null
            isLoadingSummary = false
        }
        try {
            val date = teeTime.parsedInstant() ?: Instant.now()
            val fetched = weatherService.fetchWeatherForCourse(
                placeId = teeTime.courseId,
                name = teeTime.courseName,
                forDate = date,
            )
            weather = fetched
            lastFetchedAt = Instant.now().epochSecond
            if (isSilver) {
                aiSummary = weatherService.weatherFunSummary(
                    courseName = teeTime.courseName,
                    teeTimeDate = date,
                    weather = fetched,
                )
            }
        } catch (_: Exception) {
            failed = true
            aiSummary = null
        }
        isLoading = false
        isLoadingSummary = false
    }

    val dateSubtitle = remember(teeTime.date) {
        teeTime.formattedDate().split(" • ").drop(1).joinToString(" • ")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LinksideColors.Card),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.WbSunny,
                contentDescription = null,
                tint = LinksideColors.AccentLabel,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "COURSE CONDITIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = LinksideColors.TextSecondary,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = dateSubtitle,
                style = MaterialTheme.typography.labelSmall,
                color = LinksideColors.TextTertiary,
            )
        }

        HorizontalDivider(color = LinksideColors.Muted.copy(alpha = 0.5f))

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SkeletonBar(fraction = 0.4f, height = 14.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(4) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SkeletonBar(fraction = 0.5f, height = 24.dp)
                                SkeletonBar(fraction = 0.7f, height = 12.dp)
                            }
                        }
                    }
                }
            }
            failed || weather == null -> {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = LinksideColors.TextTertiary)
                    Text(
                        "Conditions unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinksideColors.TextSecondary,
                    )
                }
            }
            else -> {
                val w = weather!!
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = teeTime.courseName,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LinksideColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (isSilver) {
                        when {
                            isLoadingSummary -> Text(
                                "AI Caddie is checking the vibes...",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = LinksideColors.TextSecondary,
                            )
                            !aiSummary.isNullOrBlank() -> Text(
                                "AI Caddie: $aiSummary",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = LinksideColors.TextSecondary,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatCell(
                            icon = conditionIcon(w.weatherCode),
                            value = "${w.temperatureF.roundToInt()}°F",
                            label = w.conditionLabel,
                            iconColor = tempIconTint(w.temperatureF),
                            modifier = Modifier.weight(1f),
                        )
                        VerticalStatDivider()
                        StatCell(
                            icon = Icons.Outlined.Air,
                            value = "${w.windSpeedMph.roundToInt()} mph",
                            label = "Wind",
                            modifier = Modifier.weight(1f),
                        )
                        VerticalStatDivider()
                        StatCell(
                            icon = Icons.Default.WaterDrop,
                            value = "${w.precipProbability}%",
                            label = "Rain",
                            iconColor = LinksideColors.RainBlue,
                            modifier = Modifier.weight(1f),
                        )
                        VerticalStatDivider()
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            val pointColor = scorePointColor(w.overallScore)
                            Box(contentAlignment = Alignment.Center) {
                                ScoreRing(score = w.overallScore, size = 38.dp, stroke = 3.dp)
                                Text(
                                    "${w.overallScore}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = pointColor,
                                )
                            }
                            Text(
                                w.scoreLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = pointColor,
                            )
                        }
                    }

                    if (w.conditionTags.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            w.conditionTags.forEach { tag ->
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = LinksideColors.TextSecondary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White.copy(alpha = 0.07f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { showBreakdown = true },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "How was this calculated?",
                                    tint = LinksideColors.TextTertiary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }

    if (showBreakdown && weather != null) {
        ConditionsBreakdownSheet(
            weather = weather!!,
            onDismiss = { showBreakdown = false },
        )
    }
}

@Composable
private fun StatCell(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    iconColor: Color = LinksideColors.AccentLabel,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        Text(value, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary, style = MaterialTheme.typography.bodyMedium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = LinksideColors.TextSecondary)
    }
}

@Composable
private fun VerticalStatDivider() {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.08f)),
    )
}

@Composable
private fun SkeletonBar(fraction: Float, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(fraction)
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(LinksideColors.Muted),
    )
}

@Composable
private fun ScoreRing(
    score: Int,
    size: androidx.compose.ui.unit.Dp,
    stroke: androidx.compose.ui.unit.Dp,
) {
    val progress = (score / 100f).coerceIn(0f, 1f)
    val gradient = Brush.sweepGradient(
        0.00f to Color(0xFFFF0000),
        0.25f to Color(0xFFFF8000),
        0.50f to Color(0xFFFFFF00),
        0.75f to Color(0xFF80FF00),
        1.00f to Color(0xFF00FF00),
    )
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = stroke.toPx()
        val diameter = this.size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)
        drawArc(
            color = Color.White.copy(alpha = 0.12f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawArc(
            brush = gradient,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

private fun scorePointColor(score: Int): Color {
    val t = score / 100.0
    val stops = listOf(
        0.00 to Color(0xFFFF0000),
        0.25 to Color(0xFFFF8000),
        0.50 to Color(0xFFFFFF00),
        0.75 to Color(0xFF80FF00),
        1.00 to Color(0xFF00FF00),
    )
    for (i in 0 until stops.lastIndex) {
        val (loT, loC) = stops[i]
        val (hiT, hiC) = stops[i + 1]
        if (t <= hiT) {
            val range = hiT - loT
            val f = if (range == 0.0) 0.0 else (t - loT) / range
            return Color(
                red = loC.red + (hiC.red - loC.red) * f.toFloat(),
                green = loC.green + (hiC.green - loC.green) * f.toFloat(),
                blue = loC.blue + (hiC.blue - loC.blue) * f.toFloat(),
            )
        }
    }
    return stops.last().second
}

@Composable
private fun tempIconTint(f: Double): Color = when {
    f >= 85 -> Color(0xFFF59E0B)
    f < 50 -> LinksideColors.RainBlue
    else -> LinksideColors.AccentLabel
}

private fun conditionIcon(code: Int): ImageVector = when (code) {
    0, 1 -> Icons.Default.WbSunny
    2 -> Icons.Default.WbCloudy
    95, in 96..99 -> Icons.Default.Thunderstorm
    in 51..67, in 80..82 -> Icons.Default.WaterDrop
    else -> Icons.Default.Cloud
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionsBreakdownSheet(
    weather: CourseWeather,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LinksideColors.Primary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "How it's calculated",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDismiss) {
                    Text("Done", color = LinksideColors.AccentLabel, fontWeight = FontWeight.SemiBold)
                }
            }

            val pointColor = scorePointColor(weather.overallScore)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LinksideColors.Card)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    ScoreRing(score = weather.overallScore, size = 64.dp, stroke = 5.dp)
                    Text(
                        "${weather.overallScore}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = pointColor,
                    )
                }
                Column {
                    Text(weather.scoreLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LinksideColors.TextPrimary)
                    Text("Overall conditions score", style = MaterialTheme.typography.bodyMedium, color = LinksideColors.TextSecondary)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "FORMULA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = LinksideColors.TextSecondary,
                )
                Text(
                    "Start at 80, then adjust for temperature, wind, rain chance, and recent dryness.",
                    style = MaterialTheme.typography.labelMedium,
                    color = LinksideColors.TextSecondary,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Score  ·  ${weather.overallScore}/100",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                )
                Text(weather.scoreLabel, style = MaterialTheme.typography.labelMedium, color = LinksideColors.TextSecondary)

                val rows = scoreRows(weather)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                ) {
                    rows.forEachIndexed { idx, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (idx % 2 == 0) LinksideColors.Card
                                    else LinksideColors.Card.copy(alpha = 0.6f),
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                row.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (idx == 0) LinksideColors.TextSecondary else LinksideColors.TextPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            if (idx == 0) {
                                Text("${row.value}", style = MaterialTheme.typography.labelMedium, color = LinksideColors.TextSecondary)
                            } else {
                                Text(
                                    row.deltaLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        row.value < 0 -> LinksideColors.Danger.copy(alpha = 0.8f)
                                        row.value == 0 -> LinksideColors.TextSecondary
                                        else -> LinksideColors.AccentLabel
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Text(
                "Estimated from Open-Meteo weather data.",
                style = MaterialTheme.typography.labelMedium,
                color = LinksideColors.TextSecondary,
            )
        }
    }
}

private data class ScoreRow(val label: String, val deltaLabel: String, val value: Int)

private fun scoreRows(weather: CourseWeather): List<ScoreRow> {
    val temp = weather.temperatureF.roundToInt()
    val tempBand = when {
        weather.temperatureF >= 110 -> "Extreme heat ($temp°F)"
        weather.temperatureF >= 100 -> "Very hot ($temp°F)"
        weather.temperatureF >= 90 -> "Hot ($temp°F)"
        weather.temperatureF >= 80 -> "Warm ($temp°F)"
        weather.temperatureF >= 70 -> "Ideal temp ($temp°F)"
        weather.temperatureF >= 60 -> "Comfortable ($temp°F)"
        weather.temperatureF >= 50 -> "Cool ($temp°F)"
        weather.temperatureF >= 40 -> "Cold ($temp°F)"
        weather.temperatureF >= 30 -> "Very cold ($temp°F)"
        else -> "Freezing ($temp°F)"
    }
    val wind = weather.windSpeedMph.roundToInt()
    val windBand = when {
        weather.windSpeedMph < 5 -> "Calm ($wind mph)"
        weather.windSpeedMph < 10 -> "Light breeze ($wind mph)"
        weather.windSpeedMph < 15 -> "Moderate ($wind mph)"
        weather.windSpeedMph < 20 -> "Breezy ($wind mph)"
        weather.windSpeedMph < 25 -> "Windy ($wind mph)"
        else -> "Very windy ($wind mph)"
    }
    val pp = weather.precipProbability
    val precipBand = when {
        pp < 10 -> "No rain expected ($pp%)"
        pp < 20 -> "Low rain chance ($pp%)"
        pp < 30 -> "Some rain chance ($pp%)"
        pp < 40 -> "Moderate rain chance ($pp%)"
        pp < 75 -> "High rain chance ($pp%)"
        pp < 100 -> "Very high rain chance ($pp%)"
        else -> "Rain certain ($pp%)"
    }
    val rain7d = String.format("%.1f", weather.rainLast7dMm)
    val bonus = weather.drySurfaceBonus
    val bonusLabel = if (bonus > 0) "Dry last 7 days ($rain7d mm)" else "Wet last 7 days ($rain7d mm)"

    fun delta(v: Int) = when {
        v > 0 -> "+$v"
        v < 0 -> "−${kotlin.math.abs(v)}"
        else -> "0"
    }

    return listOf(
        ScoreRow("Starting score", "", CourseWeather.BASELINE_SCORE),
        ScoreRow(tempBand, delta(weather.temperatureDelta), weather.temperatureDelta),
        ScoreRow(windBand, delta(weather.windDelta), weather.windDelta),
        ScoreRow(precipBand, delta(weather.precipDelta), weather.precipDelta),
        ScoreRow(bonusLabel, delta(bonus), bonus),
    )
}
