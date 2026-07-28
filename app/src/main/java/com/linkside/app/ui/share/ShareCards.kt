package com.linkside.app.ui.share

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkside.app.data.model.ScorecardShareEntry
import com.linkside.app.data.model.TeeTime
import com.linkside.app.ui.theme.LinksideColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

private val CardGreenTop = Color(0xFF0F2D1E)
private val CardGreenBottom = Color(0xFF1C4D33)
private val CardRowBg = Color(0xFF122A20)
private val CardFooterBg = Color(0xFF0D2419)
private val CardDivider = Color(0xFF1A3A2C)
private val CardStroke = Color(0xFF2A5440)
private val DarkInk = Color(0xFF0A1F16)

@Composable
fun rememberShareCardCapture(): ShareCardCapture {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) {
        ShareCardCapture(
            layerModifier = Modifier.drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                drawLayer(graphicsLayer)
            },
            captureBitmap = {
                withContext(Dispatchers.Default) {
                    graphicsLayer.toImageBitmap().asAndroidBitmap()
                        .copy(Bitmap.Config.ARGB_8888, false)
                }
            },
        )
    }
}

class ShareCardCapture(
    val layerModifier: Modifier,
    val captureBitmap: suspend () -> Bitmap,
)

@Composable
fun ScorecardShareContent(
    teeTime: TeeTime,
    players: List<ScorecardShareEntry>,
    recap: String?,
    modifier: Modifier = Modifier,
) {
    val dateString = remember(teeTime.date) { formatShareDate(teeTime, long = true) }
    val winnerLabel = remember(players) {
        val leader = players.firstOrNull() ?: return@remember null
        val tied = players.count { it.total == leader.total } > 1
        if (tied) null else "${leader.name} leads with ${leader.total} 🏆"
    }
    val teaser = remember(recap) {
        val trimmed = recap?.trim().orEmpty()
        when {
            trimmed.isEmpty() -> null
            trimmed.length > 180 -> trimmed.take(180).trimEnd() + "…"
            else -> trimmed
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, LinksideColors.Accent.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .background(CardRowBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(CardGreenTop, CardGreenBottom)))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = LinksideColors.Accent, modifier = Modifier.size(14.dp))
                Text(
                    "ROUND RESULTS",
                    color = LinksideColors.Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                )
            }
            Text(teeTime.courseName, color = LinksideColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(dateString, color = LinksideColors.TextSecondary, fontSize = 14.sp)
            if (winnerLabel != null) {
                Text(winnerLabel, color = LinksideColors.Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(color = CardStroke)

        if (players.isEmpty()) {
            Text(
                "No scores recorded yet.",
                color = LinksideColors.TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                textAlign = TextAlign.Center,
            )
        } else {
            players.forEachIndexed { index, player ->
                val isFirst = index == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isFirst) LinksideColors.Accent.copy(alpha = 0.08f) else CardRowBg)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isFirst) LinksideColors.Accent else LinksideColors.Muted),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            color = if (isFirst) DarkInk else LinksideColors.TextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    PlayerAvatar(name = player.name, size = 32.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            player.name,
                            color = LinksideColors.TextPrimary,
                            fontWeight = if (isFirst) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        highlightText(player)?.let {
                            Text(it, color = LinksideColors.TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Text(
                        "${player.total}",
                        color = if (isFirst) LinksideColors.Accent else LinksideColors.TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (index < players.lastIndex) {
                    HorizontalDivider(color = CardDivider, modifier = Modifier.padding(start = 80.dp))
                }
            }
        }

        if (teaser != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F2416))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LinksideColors.Accent, modifier = Modifier.size(14.dp))
                Text(teaser, color = LinksideColors.TextSecondary, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }

        InstallFooter()
    }
}

@Composable
fun PlayerOfTheDayContent(
    teeTime: TeeTime,
    winner: ScorecardShareEntry,
    modifier: Modifier = Modifier,
) {
    val dateString = remember(teeTime.date) { formatShareDate(teeTime, long = false) }
    val highlight = remember(winner) {
        buildList {
            if (winner.eaglesOrBetter > 0) {
                add("${winner.eaglesOrBetter} eagle${if (winner.eaglesOrBetter == 1) "" else "s"}+")
            }
            if (winner.birdies > 0) {
                add("${winner.birdies} birdie${if (winner.birdies == 1) "" else "s"}")
            }
        }.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, LinksideColors.Gold.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(CardGreenTop, CardGreenBottom)))
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "PLAYER OF THE DAY",
                color = LinksideColors.Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp,
            )
            if (!winner.isComplete) {
                Text(
                    "IN PROGRESS · THRU ${winner.holesPlayed}",
                    color = LinksideColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                )
            }
            Box(contentAlignment = Alignment.BottomEnd) {
                PlayerAvatar(name = winner.name, size = 92.dp, goldRing = true)
                // offset (not negative padding — Compose throws on negative padding)
                Text("🏆", fontSize = 30.sp, modifier = Modifier.offset(x = 6.dp, y = 6.dp))
            }
            Text(
                winner.name,
                color = LinksideColors.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${winner.total}", color = LinksideColors.Gold, fontSize = 52.sp, fontWeight = FontWeight.Black)
                Text(
                    if (winner.isComplete) "strokes" else "thru ${winner.holesPlayed}",
                    color = LinksideColors.TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            if (highlight != null) {
                Text(
                    highlight,
                    color = LinksideColors.Accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LinksideColors.Accent.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
            Text(
                "${teeTime.courseName} · $dateString",
                color = LinksideColors.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
        InstallFooter()
    }
}

@Composable
private fun InstallFooter() {
    val qr = remember { generateQrBitmap(ShareLinks.INSTALL_URL, sizePx = 256).asImageBitmap() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardFooterBg)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            bitmap = qr,
            contentDescription = "Install Linkside",
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(5.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Get Linkside — free", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Scan to play more golf", color = LinksideColors.TextTertiary, fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Linkside", color = LinksideColors.Accent, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Text("Play more golf.", color = LinksideColors.TextTertiary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PlayerAvatar(name: String, size: androidx.compose.ui.unit.Dp, goldRing: Boolean = false) {
    val hue = remember(name) {
        (name.sumOf { it.code }.absoluteValue % 360).toFloat()
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (goldRing) Modifier.border(3.dp, LinksideColors.Gold, CircleShape) else Modifier,
            )
            .background(Color.hsl(hue, 0.55f, 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            color = Color.White,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun highlightText(player: ScorecardShareEntry): String? {
    val bits = buildList {
        if (player.eaglesOrBetter > 0) {
            add("${player.eaglesOrBetter} eagle${if (player.eaglesOrBetter == 1) "" else "s"}+")
        }
        if (player.birdies > 0) {
            add("${player.birdies} birdie${if (player.birdies == 1) "" else "s"}")
        }
    }
    if (bits.isNotEmpty()) return bits.joinToString(" · ")
    val f = player.front9
    val b = player.back9
    return if (f != null && b != null) "F9 $f · B9 $b" else null
}

private fun formatShareDate(teeTime: TeeTime, long: Boolean): String {
    val instant = teeTime.parsedInstant() ?: return teeTime.date
    val zoned = instant.atZone(ZoneId.systemDefault())
    val pattern = if (long) "EEEE, MMM d, yyyy" else "EEEE, MMM d"
    return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(zoned)
}
