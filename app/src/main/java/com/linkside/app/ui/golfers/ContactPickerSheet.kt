package com.linkside.app.ui.golfers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.Friend
import com.linkside.app.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerSheet(
    contacts: List<Friend>,
    selectedPhones: Set<String>,
    onToggle: (String) -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Select golfers", fontWeight = FontWeight.SemiBold)
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .padding(vertical = 8.dp),
            ) {
                items(contacts, key = { it.phone }) { contact ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = selectedPhones.contains(contact.phone),
                            onCheckedChange = { onToggle(contact.phone) },
                        )
                        Column {
                            Text(contact.fullName)
                            Text(contact.phone, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            PrimaryButton(title = "Done", onClick = onDone)
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
