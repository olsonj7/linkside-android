package com.linkside.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.GolfTrip
import com.linkside.app.data.model.TeeTime
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun DeclinesSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = LinksideColors.TextSecondary,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
    )
}

@Composable
fun DeclinedTeeTimeRow(
    teeTime: TeeTime,
    upcoming: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LinksideColors.Card)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Cancel,
            contentDescription = null,
            tint = LinksideColors.Danger,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                teeTime.courseName,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                teeTime.formattedDate(),
                color = LinksideColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = if (upcoming) "Upcoming" else "Recent",
            color = LinksideColors.Danger,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(LinksideColors.Danger.copy(alpha = 0.14f))
                .padding(horizontal = 8.dp, vertical = 5.dp),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LinksideColors.TextSecondary,
        )
    }
}

@Composable
fun DeclinedTripRow(
    trip: GolfTrip,
    upcoming: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LinksideColors.Card)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Cancel,
            contentDescription = null,
            tint = LinksideColors.Danger,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                trip.name,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                "${trip.location} · ${trip.formattedDateRange()}",
                color = LinksideColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = if (upcoming) "Upcoming" else "Recent",
            color = LinksideColors.Danger,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(LinksideColors.Danger.copy(alpha = 0.14f))
                .padding(horizontal = 8.dp, vertical = 5.dp),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LinksideColors.TextSecondary,
        )
    }
}
