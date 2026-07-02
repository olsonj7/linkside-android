package com.linkside.app.ui.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.linkside.app.ui.components.themeCardShape
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCard(
    trip: GolfTrip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasBorder = LinksideColors.CardBorder.alpha > 0f
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .themeCardShape(14.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hasBorder) 0.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LinksideColors.AccentChipBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Flight,
                    contentDescription = null,
                    tint = LinksideColors.AccentLabel,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    trip.name,
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                )
                Text(
                    text = "${trip.location} · ${trip.formattedDateRange()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinksideColors.TextSecondary,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text(
                    text = "${trip.yesCount}/${trip.golfersNeeded} confirmed",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinksideColors.AccentLabel,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LinksideColors.TextTertiary,
            )
        }
    }
}
