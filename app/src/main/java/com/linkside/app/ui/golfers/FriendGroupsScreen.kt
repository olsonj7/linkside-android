package com.linkside.app.ui.golfers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.Friend
import com.linkside.app.data.model.FriendGroup
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendGroupsScreen(
    groups: List<FriendGroup>,
    canCreateGroup: Boolean,
    onBack: () -> Unit,
    onCreateGroup: () -> Unit,
    onEditGroup: (FriendGroup) -> Unit,
    onDeleteGroup: (FriendGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<FriendGroup?>(null) }

    pendingDelete?.let { group ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Group") },
            text = { Text("Delete \"${group.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteGroup(group)
                        pendingDelete = null
                    },
                ) {
                    Text("Delete", color = LinksideColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            TopAppBar(
                title = { Text("Friend Groups") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = LinksideColors.Primary,
                    titleContentColor = LinksideColors.TextPrimary,
                    navigationIconContentColor = LinksideColors.TextPrimary,
                ),
            )
        },
        floatingActionButton = {
            if (canCreateGroup) {
                FloatingActionButton(
                    onClick = onCreateGroup,
                    containerColor = LinksideColors.Gold,
                    contentColor = LinksideColors.OnGold,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add group")
                }
            }
        },
    ) { padding ->
        if (groups.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No groups yet", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                Text(
                    "Create a group to quickly invite the same friends to tee times.",
                    color = LinksideColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                )
                PrimaryButton(
                    title = if (canCreateGroup) "Create Group" else "Limit reached (3 max)",
                    onClick = onCreateGroup,
                    enabled = canCreateGroup,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(groups, key = { it.id }) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditGroup(group) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = LinksideColors.Card),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(group.name, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                                Text(
                                    "${group.members.size} member${if (group.members.size == 1) "" else "s"}",
                                    color = LinksideColors.TextSecondary,
                                )
                            }
                            IconButton(onClick = { pendingDelete = group }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LinksideColors.Danger)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreen(
    group: FriendGroup?,
    savedGolfers: List<Friend>,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: (name: String, members: List<Friend>) -> Unit,
    modifier: Modifier = Modifier,
    isLoadingGolfers: Boolean = false,
    onRefreshGolfers: () -> Unit = {},
    contactStatuses: Map<String, com.linkside.app.data.model.ContactStatus> = emptyMap(),
) {
    var name by remember { mutableStateOf(group?.name.orEmpty()) }
    var selectedPhones by remember {
        mutableStateOf(group?.members?.map { it.phone }?.toSet() ?: emptySet())
    }
    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (savedGolfers.isEmpty()) onRefreshGolfers()
    }

    // Keep existing group members selectable even if they're not in My Golfers yet.
    val candidateGolfers = remember(savedGolfers, group) {
        val byPhone = linkedMapOf<String, Friend>()
        group?.members?.forEach { byPhone[it.phone] = it }
        savedGolfers.forEach { byPhone[it.phone] = it }
        byPhone.values.sortedBy { it.fullName.lowercase() }
    }
    val selectedMembers = candidateGolfers.filter { selectedPhones.contains(it.phone) }

    if (showPicker) {
        ContactPickerSheet(
            contacts = candidateGolfers,
            selectedPhones = selectedPhones,
            contactStatuses = contactStatuses,
            onToggle = { phone ->
                selectedPhones = if (selectedPhones.contains(phone)) {
                    selectedPhones - phone
                } else {
                    selectedPhones + phone
                }
            },
            onDone = { showPicker = false },
            onDismiss = { showPicker = false },
            isLoading = isLoadingGolfers && candidateGolfers.isEmpty(),
            emptyMessage = if (savedGolfers.isEmpty()) {
                "No golfers yet. Add friends on the Golfers tab first, then come back to build this group."
            } else {
                "No golfers available."
            },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            TopAppBar(
                title = { Text(if (group == null) "New Group" else "Edit Group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = LinksideColors.Primary,
                    titleContentColor = LinksideColors.TextPrimary,
                    navigationIconContentColor = LinksideColors.TextPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Group name") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Members", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                TextButton(
                    onClick = {
                        onRefreshGolfers()
                        showPicker = true
                    },
                ) {
                    Text("Add from Golfers", color = LinksideColors.AccentLabel)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (selectedMembers.isEmpty()) {
                    item {
                        Text(
                            "No members added yet.",
                            color = LinksideColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                } else {
                    items(selectedMembers, key = { it.phone }) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(friend.fullName, color = LinksideColors.TextPrimary)
                                Text(
                                    friend.phone,
                                    color = LinksideColors.TextTertiary,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            IconButton(
                                onClick = { selectedPhones = selectedPhones - friend.phone },
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = LinksideColors.TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            PrimaryButton(
                title = when {
                    isSaving -> "Saving…"
                    group == null -> "Create Group"
                    else -> "Save Changes"
                },
                onClick = {
                    val members = candidateGolfers.filter { selectedPhones.contains(it.phone) }
                    onSave(name.trim(), members)
                },
                enabled = name.trim().isNotEmpty() && !isSaving,
            )
        }
    }
}

@Composable
fun ManualGolferDialog(
    onDismiss: () -> Unit,
    onAdd: (Friend) -> Unit,
) {
    var phone by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Golfer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First name") }, singleLine = true)
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last name") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAdd(
                        Friend(
                            phone = phone.trim(),
                            firstName = firstName.trim().ifEmpty { "Golfer" },
                            lastName = lastName.trim(),
                        ),
                    )
                    onDismiss()
                },
                enabled = com.linkside.app.data.api.PhoneUtils.isValidPhone(phone),
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
