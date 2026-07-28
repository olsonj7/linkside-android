package com.linkside.app.ui.golfers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.data.model.ContactStatus
import com.linkside.app.data.model.Friend
import com.linkside.app.ui.components.LinksideWordmark
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GolfersScreen(
    golfers: List<Friend>,
    contactStatuses: Map<String, ContactStatus>,
    isLoading: Boolean,
    isPreparingInvite: Boolean,
    inviteError: String?,
    onClearInviteError: () -> Unit,
    onRefresh: () -> Unit,
    onOpenGroups: () -> Unit,
    onAddFromContacts: () -> Unit,
    onAddManual: () -> Unit,
    onRemove: (Friend) -> Unit,
    onInviteToApp: (Friend) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchText by remember { mutableStateOf("") }

    val filtered = remember(golfers, contactStatuses, searchText) {
        val sorted = golfers.sortedWith(
            compareBy<Friend> { statusSortKey(contactStatuses, it.phone) }
                .thenBy { it.fullName.lowercase() },
        )
        if (searchText.isBlank()) {
            sorted
        } else {
            val q = searchText.trim()
            sorted.filter {
                it.fullName.contains(q, ignoreCase = true) || it.phone.contains(q)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = LinksideColors.Primary,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        LinksideWordmark(
                            fontSize = 20,
                            textAlign = TextAlign.Center,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onRefresh, enabled = !isLoading) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = LinksideColors.AccentLabel,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = LinksideColors.Primary,
                        titleContentColor = LinksideColors.TextPrimary,
                        actionIconContentColor = LinksideColors.AccentLabel,
                    ),
                    actions = {
                        IconButton(onClick = onOpenGroups) {
                            Icon(Icons.Default.Group, contentDescription = "Friend groups")
                        }
                        IconButton(onClick = onAddManual) {
                            Icon(Icons.Default.GroupAdd, contentDescription = "Add golfer manually")
                        }
                        IconButton(onClick = onAddFromContacts) {
                            Icon(Icons.Default.Add, contentDescription = "Add golfers from contacts")
                        }
                    },
                )
            },
        ) { padding ->
            if (golfers.isEmpty() && !isLoading) {
                EmptyGolfersState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onAddFromContacts = onAddFromContacts,
                    onAddManual = onAddManual,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Text(
                            text = "My Golfers",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = LinksideColors.TextPrimary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Search golfers") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = LinksideColors.TextTertiary,
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LinksideColors.Card,
                                unfocusedContainerColor = LinksideColors.Card,
                                focusedBorderColor = LinksideColors.Muted,
                                unfocusedBorderColor = LinksideColors.Muted,
                                focusedTextColor = LinksideColors.TextPrimary,
                                unfocusedTextColor = LinksideColors.TextPrimary,
                                cursorColor = LinksideColors.AccentLabel,
                            ),
                        )
                    }

                    item {
                        StatusLegendRow(golferCount = golfers.size)
                    }

                    items(filtered, key = { it.phone }) { friend ->
                        val status = contactStatusFor(contactStatuses, friend.phone)
                        val onLinkside = status?.isOnLinkside == true
                        GolferSwipeRow(
                            friend = friend,
                            onLinkside = onLinkside,
                            canInvite = !onLinkside,
                            onInvite = { onInviteToApp(friend) },
                            onRemove = { onRemove(friend) },
                        )
                    }

                    item { Spacer(modifier = Modifier.size(24.dp)) }
                }
            }
        }

        if (isPreparingInvite) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LinksideColors.Primary.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = LinksideColors.AccentLabel)
            }
        }
    }

    if (inviteError != null) {
        AlertDialog(
            onDismissRequest = onClearInviteError,
            title = { Text("Couldn't Send Invite") },
            text = { Text(inviteError) },
            confirmButton = {
                TextButton(onClick = onClearInviteError) {
                    Text("OK", color = LinksideColors.AccentLabel)
                }
            },
        )
    }
}

@Composable
private fun EmptyGolfersState(
    onAddFromContacts: () -> Unit,
    onAddManual: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(LinksideColors.AccentLabel.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                tint = LinksideColors.AccentLabel,
                modifier = Modifier.size(44.dp),
            )
        }
        Text(
            "Add Your Golf Crew",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LinksideColors.TextPrimary,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            "Linkside works from your contacts. Add golfers here so you can quickly invite them to tee times, trips, and tournaments.",
            color = LinksideColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp, bottom = 24.dp),
        )
        TextButton(onClick = onAddFromContacts) {
            Text("Add Golfers from Contacts", color = LinksideColors.AccentLabel)
        }
        TextButton(onClick = onAddManual) {
            Text("Add manually", color = LinksideColors.TextSecondary)
        }
    }
}

@Composable
private fun StatusLegendRow(golferCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(
            icon = Icons.Default.CheckCircle,
            label = "Linkside User",
            tint = LinksideColors.AccentLabel,
        )
        LegendItem(
            icon = Icons.Default.Cancel,
            label = "Not on Linkside",
            tint = LinksideColors.TextTertiary,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "$golferCount golfers",
            style = MaterialTheme.typography.labelSmall,
            color = LinksideColors.TextSecondary,
        )
    }
}

@Composable
private fun LegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = LinksideColors.TextSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GolferSwipeRow(
    friend: Friend,
    onLinkside: Boolean,
    canInvite: Boolean,
    onInvite: () -> Unit,
    onRemove: () -> Unit,
) {
    val currentRemove by rememberUpdatedState(onRemove)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    currentRemove()
                    true
                }
                else -> false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val target = dismissState.targetValue
            when (target) {
                SwipeToDismissBoxValue.EndToStart -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LinksideColors.Danger)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Remove", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                            Icon(Icons.Default.Delete, contentDescription = null, tint = LinksideColors.TextPrimary)
                        }
                    }
                }
                else -> Spacer(modifier = Modifier.fillMaxSize())
            }
        },
    ) {
        GolferRow(
            friend = friend,
            onLinkside = onLinkside,
            onInvite = if (canInvite) onInvite else null,
        )
    }
}

@Composable
private fun GolferRow(
    friend: Friend,
    onLinkside: Boolean,
    onInvite: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatarView(name = friend.fullName, size = 44.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    friend.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (onLinkside) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (onLinkside) LinksideColors.AccentLabel else LinksideColors.TextTertiary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = if (onLinkside) "On Linkside" else "Not on Linkside",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (onLinkside) LinksideColors.AccentLabel else LinksideColors.TextTertiary,
                    )
                }
            }
            if (onInvite != null) {
                IconButton(onClick = onInvite) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Invite to app",
                        tint = LinksideColors.AccentLabel,
                    )
                }
            }
        }
    }
}

private fun contactStatusFor(
    statuses: Map<String, ContactStatus>,
    phone: String,
): ContactStatus? {
    val normalized = PhoneUtils.normalizePhone(phone)
    return statuses[normalized] ?: statuses[phone]
}

private fun statusSortKey(statuses: Map<String, ContactStatus>, phone: String): Int {
    val status = contactStatusFor(statuses, phone)
    return when {
        status?.registered == true -> 0
        status?.optedIn == true -> 1
        else -> 2
    }
}
