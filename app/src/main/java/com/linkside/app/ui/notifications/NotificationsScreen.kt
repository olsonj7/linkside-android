package com.linkside.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.AppNotification
import com.linkside.app.ui.components.LinkButton
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.theme.LinksideColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(
                title = "Notifications",
                onBack = onBack,
                actions = {
                    if (notifications.any { !it.read }) {
                        LinkButton(title = "Mark all read", onClick = onMarkAllRead)
                    }
                },
            )
        },
    ) { padding ->
        if (notifications.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
            ) {
                Text("No notifications yet.", color = LinksideColors.TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationRow(
                        notification = notification,
                        onClick = { onNotificationClick(notification) },
                        onDelete = { onDelete(notification.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: AppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("MMM d · h:mm a", Locale.getDefault())
    val time = Instant.ofEpochMilli(notification.createdAt.toLong())
        .atZone(ZoneId.systemDefault())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (notification.read) LinksideColors.Card else LinksideColors.Accent.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(notification.title, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary, modifier = Modifier.weight(1f))
            LinkButton(title = "Delete", onClick = onDelete)
        }
        Text(notification.body, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(formatter.format(time), style = MaterialTheme.typography.labelSmall, color = LinksideColors.TextTertiary)
    }
}
