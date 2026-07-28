package com.linkside.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.theme.LinksideColors

/**
 * Bottom sheet for composing a new poll. Used by both idea threads and golf-trip
 * chats; the caller performs the context-specific create call in [onCreate].
 * Mirrors iOS `CreatePollSheet`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollSheet(
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (question: String, options: List<String>, allowMultiple: Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    var allowMultiple by remember { mutableStateOf(false) }

    val trimmedOptions = options.map { it.trim() }.filter { it.isNotEmpty() }
    val canCreate = question.trim().isNotEmpty() && trimmedOptions.size >= 2 && !isCreating

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LinksideColors.Primary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "New Poll",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = LinksideColors.TextSecondary)
                }
            }

            Text(
                text = "QUESTION",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextSecondary,
            )
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What should we decide?") },
                maxLines = 3,
            )

            Text(
                text = "OPTIONS",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextSecondary,
            )
            options.forEachIndexed { index, value ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { options[index] = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Option ${index + 1}") },
                        singleLine = true,
                    )
                    if (options.size > 2) {
                        IconButton(onClick = { options.removeAt(index) }) {
                            Icon(
                                Icons.Default.RemoveCircle,
                                contentDescription = "Remove option",
                                tint = LinksideColors.Danger,
                            )
                        }
                    }
                }
            }
            if (options.size < 8) {
                TextButton(onClick = { options.add("") }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = LinksideColors.AccentLabel)
                    Text("  Add option", color = LinksideColors.AccentLabel)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Allow multiple choices",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = LinksideColors.TextPrimary,
                    )
                    Text(
                        text = "Voters can pick more than one option",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = LinksideColors.TextTertiary,
                    )
                }
                Switch(checked = allowMultiple, onCheckedChange = { allowMultiple = it })
            }

            PrimaryButton(
                title = if (isCreating) "Posting…" else "Post Poll",
                onClick = { onCreate(question.trim(), trimmedOptions, allowMultiple) },
                enabled = canCreate,
                modifier = Modifier.fillMaxWidth(),
            )
            if (isCreating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = LinksideColors.Accent, strokeWidth = 2.dp)
                }
            }
        }
    }
}
