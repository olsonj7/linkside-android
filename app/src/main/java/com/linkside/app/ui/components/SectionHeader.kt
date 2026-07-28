package com.linkside.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    accentColor: Color = LinksideColors.Accent,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .background(accentColor, RoundedCornerShape(50)),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            ),
            color = LinksideColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    color = LinksideColors.AccentLabel,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/** In-app wordmark — matches iOS `LinksideWordmark` (flag pennant on the leading "l"). */
@Composable
fun LinksideWordmark(
    modifier: Modifier = Modifier,
    fontSize: Int = 20,
    color: Color = LinksideColors.TextPrimary,
    textAlign: TextAlign = TextAlign.Center,
) {
    val flagColor = LinksideColors.Accent
    val alignment = when (textAlign) {
        TextAlign.Start -> Alignment.CenterStart
        TextAlign.End -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    Box(
        modifier = modifier,
        contentAlignment = alignment,
    ) {
        // Size the overlay to the text itself (same as iOS `.overlay(alignment: .topLeading)`).
        Box {
            Text(
                text = "linkside",
                fontSize = fontSize.sp,
                // iOS: .system(size:weight:.semibold, design:.rounded)
                // Android has no SF Rounded; SansSerif + SemiBold is the system stand-in.
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                color = color,
                letterSpacing = (-0.3).sp,
            )
            Canvas(modifier = Modifier.matchParentSize()) {
                val fs = fontSize.sp.toPx()
                // Ratios copied from iOS LinksideWordmark
                val x = fs * 0.16f
                val y = fs * 0.05f
                val w = fs * 0.26f
                val h = fs * 0.18f
                val path = Path().apply {
                    moveTo(x, y)
                    lineTo(x + w, y + h / 2f)
                    lineTo(x, y + h)
                    close()
                }
                drawPath(path, color = flagColor)
            }
        }
    }
}

/** Standalone flag-pin mark — matches iOS `PinMark` / app-icon motif. */
@Composable
fun LinksidePinMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    poleColor: Color = LinksideColors.TextPrimary,
    flagColor: Color = LinksideColors.Accent,
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        val poleX = s * 0.36f
        val sw = s * 0.09f
        drawLine(
            color = poleColor,
            start = Offset(poleX, s * 0.10f),
            end = Offset(poleX, s * 0.90f),
            strokeWidth = sw,
            cap = StrokeCap.Round,
        )
        val flag = Path().apply {
            moveTo(poleX + sw * 0.5f, s * 0.10f)
            lineTo(s * 0.84f, s * 0.28f)
            lineTo(poleX + sw * 0.5f, s * 0.46f)
            close()
        }
        drawPath(flag, color = flagColor)
    }
}

/** Badge-style mark for splash / large branding — matches iOS `BadgeFlagMark`. */
@Composable
fun LinksideBadgeMark(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    val bg = LinksideColors.Primary
    val flagColor = LinksideColors.Accent
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.08f
        val poleX = s * 0.37f
        drawRoundRect(
            color = bg,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.24f),
        )
        drawLine(
            color = Color.White,
            start = Offset(poleX, s * 0.18f),
            end = Offset(poleX, s * 0.84f),
            strokeWidth = sw,
            cap = StrokeCap.Round,
            // stroke join unused for line
        )
        val flag = Path().apply {
            moveTo(poleX + sw * 0.5f, s * 0.18f)
            lineTo(s * 0.78f, s * 0.34f)
            lineTo(poleX + sw * 0.5f, s * 0.50f)
            close()
        }
        drawPath(flag, color = flagColor)
    }
}
