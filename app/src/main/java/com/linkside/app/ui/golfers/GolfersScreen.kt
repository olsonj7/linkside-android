package com.linkside.app.ui.golfers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.ContactStatus
import com.linkside.app.data.model.Friend
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GolfersScreen(
    golfers: List<Friend>,
    contactStatuses: Map<String, ContactStatus>,
    isLoading: Boolean,
    onOpenGroups: () -> Unit,
    onAddFromContacts: () -> Unit,
    onAddManual: () -> Unit,
    onRemove: (Friend) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            TopAppBar(
                title = { Text("My Golfers", color = LinksideColors.TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LinksideColors.Primary,
                    titleContentColor = LinksideColors.TextPrimary,
                    actionIconContentColor = LinksideColors.TextPrimary,
                ),
                actions = {
                    IconButton(onClick = onOpenGroups) {
                        Icon(Icons.Default.Group, contentDescription = "Friend groups")
                    }
                    IconButton(onClick = onAddManual) {
                        Icon(Icons.Default.Add, contentDescription = "Add golfer")
                    }
                },
            )
        },
    ) { padding ->
        if (golfers.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No golfers yet", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                Text(
                    "Add friends from contacts or enter them manually.",
                    color = LinksideColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                )
                androidx.compose.material3.TextButton(onClick = onAddFromContacts) {
                    Text("Import from contacts", color = LinksideColors.AccentLabel)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${golfers.size} golfers", color = LinksideColors.TextSecondary)
                        androidx.compose.material3.TextButton(onClick = onAddManual) {
                            Text("Add", color = LinksideColors.AccentLabel)
                        }
                    }
                }
                items(golfers, key = { it.phone }) { friend ->
                    val status = contactStatuses[friend.phone]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ProfileAvatarView(name = friend.fullName, size = 44.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(friend.fullName, fontWeight = FontWeight.Medium, color = LinksideColors.TextPrimary)
                                Text(friend.phone, style = MaterialTheme.typography.bodySmall, color = LinksideColors.TextSecondary)
                                status?.let {
                                    Text(it.label, style = MaterialTheme.typography.labelSmall, color = LinksideColors.AccentLabel)
                                }
                            }
                            IconButton(onClick = { onRemove(friend) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = LinksideColors.Danger)
                            }
                        }
                    }
                }
            }
        }
    }
}
