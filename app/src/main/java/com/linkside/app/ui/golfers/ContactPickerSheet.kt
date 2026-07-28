package com.linkside.app.ui.golfers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.data.model.ContactStatus
import com.linkside.app.data.model.Friend
import com.linkside.app.ui.components.ProfileAvatarView
import com.linkside.app.ui.theme.LinksideColors

private enum class GolferFilter { All, OnLinkside }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerSheet(
    contacts: List<Friend>,
    selectedPhones: Set<String>,
    onToggle: (String) -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
    emptyMessage: String = "No golfers available.",
    isLoading: Boolean = false,
    contactStatuses: Map<String, ContactStatus> = emptyMap(),
    onSelectAll: ((phones: List<String>, allSelected: Boolean) -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchText by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(GolferFilter.All) }

    fun statusFor(phone: String): ContactStatus? =
        contactStatuses[PhoneUtils.normalizePhone(phone)] ?: contactStatuses[phone]

    val onLinksideCount = remember(contacts, contactStatuses) {
        contacts.count { statusFor(it.phone)?.isOnLinkside == true }
    }

    val visible = remember(contacts, contactStatuses, searchText, filter) {
        contacts
            .asSequence()
            .filter { filter == GolferFilter.All || statusFor(it.phone)?.isOnLinkside == true }
            .filter { searchText.isBlank() || it.fullName.contains(searchText.trim(), ignoreCase = true) }
            // Linkside users first, then alphabetical.
            .sortedWith(
                compareByDescending<Friend> { statusFor(it.phone)?.isOnLinkside == true }
                    .thenBy { it.fullName.lowercase() },
            )
            .toList()
    }

    val visiblePhones = remember(visible) { visible.map { it.phone } }
    val allVisibleSelected = visiblePhones.isNotEmpty() && visiblePhones.all { it in selectedPhones }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LinksideColors.Primary,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        if (onSelectAll != null) {
                            onSelectAll(visiblePhones, allVisibleSelected)
                        } else {
                            visiblePhones.forEach { phone ->
                                val selected = phone in selectedPhones
                                if (allVisibleSelected && selected) onToggle(phone)
                                if (!allVisibleSelected && !selected) onToggle(phone)
                            }
                        }
                    },
                    enabled = visiblePhones.isNotEmpty(),
                ) {
                    Text(
                        if (allVisibleSelected) "Deselect All" else "Select All",
                        color = LinksideColors.AccentLabel,
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDone) {
                    Text("Done", color = LinksideColors.AccentLabel, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    label = "All",
                    selected = filter == GolferFilter.All,
                    onClick = { filter = GolferFilter.All },
                )
                FilterChip(
                    label = "On Linkside",
                    selected = filter == GolferFilter.OnLinkside,
                    onClick = { filter = GolferFilter.OnLinkside },
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                Text(
                    "$onLinksideCount on Linkside",
                    style = MaterialTheme.typography.labelMedium,
                    color = LinksideColors.TextSecondary,
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = LinksideColors.AccentLabel)
                    }
                }
                contacts.isEmpty() -> {
                    Text(
                        emptyMessage,
                        color = LinksideColors.TextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 420.dp)
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(visible, key = { it.phone }) { contact ->
                            GolferPickRow(
                                friend = contact,
                                selected = contact.phone in selectedPhones,
                                onLinkside = statusFor(contact.phone)?.isOnLinkside == true,
                                onClick = { onToggle(contact.phone) },
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                placeholder = { Text("Search golfers", color = LinksideColors.TextTertiary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = LinksideColors.TextTertiary)
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
    }
}

@Composable
private fun GolferPickRow(
    friend: Friend,
    selected: Boolean,
    onLinkside: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileAvatarView(name = friend.fullName, size = 44.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    friend.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                )
                friend.phoneLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = LinksideColors.OnGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(LinksideColors.AccentLabel.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (onLinkside) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (onLinkside) LinksideColors.AccentLabel else LinksideColors.TextTertiary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = if (onLinkside) "Linkside User" else "Not on Linkside",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (onLinkside) LinksideColors.AccentLabel else LinksideColors.TextTertiary,
                )
            }
        }
        Icon(
            imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (selected) "Selected" else "Not selected",
            tint = if (selected) LinksideColors.AccentLabel else LinksideColors.TextSecondary,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) LinksideColors.AccentLabel else LinksideColors.Muted)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        color = if (selected) LinksideColors.OnGold else LinksideColors.TextPrimary,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelMedium,
    )
}
