package com.linkside.app.ui.ideas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.data.model.Friend
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIdeaThreadScreen(
    golfers: List<Friend>,
    isCreating: Boolean,
    onBack: () -> Unit,
    onCreate: (name: String, phones: List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var selectedPhones by remember { mutableStateOf(setOf<String>()) }
    // Match iOS: only a non-empty name is required; invitees are optional.
    val canCreate = name.trim().isNotEmpty() && !isCreating

    fun togglePhone(rawPhone: String) {
        val phone = PhoneUtils.normalizePhone(rawPhone)
        selectedPhones = if (phone in selectedPhones) {
            selectedPhones - phone
        } else {
            selectedPhones + phone
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = { LinksideTopAppBar(title = "New Idea Thread", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Thread name") },
                placeholder = { Text("e.g. Saturday morning crew") },
                singleLine = true,
                enabled = !isCreating,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LinksideColors.TextPrimary,
                    unfocusedTextColor = LinksideColors.TextPrimary,
                    focusedBorderColor = LinksideColors.Accent,
                    unfocusedBorderColor = LinksideColors.Muted,
                    cursorColor = LinksideColors.Accent,
                    focusedLabelColor = LinksideColors.AccentLabel,
                    unfocusedLabelColor = LinksideColors.TextSecondary,
                    focusedPlaceholderColor = LinksideColors.TextTertiary,
                    unfocusedPlaceholderColor = LinksideColors.TextTertiary,
                    focusedContainerColor = LinksideColors.Card,
                    unfocusedContainerColor = LinksideColors.Card,
                ),
            )

            Text("Invite golfers", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
            Text(
                "Optional — you can start a thread and invite people later.",
                style = MaterialTheme.typography.bodySmall,
                color = LinksideColors.TextSecondary,
            )

            if (golfers.isEmpty()) {
                Text("Add golfers on the Golfers tab first.", color = LinksideColors.TextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(golfers, key = { it.phone }) { friend ->
                        val normalized = PhoneUtils.normalizePhone(friend.phone)
                        val checked = normalized in selectedPhones
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isCreating) { togglePhone(friend.phone) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { togglePhone(friend.phone) },
                                enabled = !isCreating,
                            )
                            Column {
                                Text(friend.fullName, color = LinksideColors.TextPrimary)
                                Text(
                                    friend.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LinksideColors.TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            PrimaryButton(
                title = if (isCreating) "Creating…" else "Create Thread",
                onClick = { onCreate(name.trim(), selectedPhones.toList()) },
                enabled = canCreate,
            )
        }
    }
}
