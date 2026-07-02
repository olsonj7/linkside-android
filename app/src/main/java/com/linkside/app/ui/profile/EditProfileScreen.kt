package com.linkside.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.linkside.app.data.model.User
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    user: User,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSave: (
        firstName: String,
        lastName: String,
        address: String,
        city: String,
        state: String,
        zipCode: String,
        handicapText: String,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    var firstName by remember(user.id) { mutableStateOf(user.firstName.orEmpty()) }
    var lastName by remember(user.id) { mutableStateOf(user.lastName.orEmpty()) }
    var address by remember(user.id) { mutableStateOf(user.address.orEmpty()) }
    var city by remember(user.id) { mutableStateOf(user.city.orEmpty()) }
    var stateField by remember(user.id) { mutableStateOf(user.state.orEmpty()) }
    var zipCode by remember(user.id) { mutableStateOf(user.zipCode.orEmpty()) }
    var handicapText by remember(user.id) {
        mutableStateOf(user.handicap?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }.orEmpty())
    }

    val parsedHandicap = handicapText.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
    val canSave = firstName.isNotBlank() &&
        lastName.isNotBlank() &&
        (handicapText.isBlank() || parsedHandicap != null)

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(title = "Edit Profile", onBack = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Name", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                EditProfileField(value = firstName, onValueChange = { firstName = it }, placeholder = "First name")
                EditProfileField(value = lastName, onValueChange = { lastName = it }, placeholder = "Last name")
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Home Address", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                EditProfileField(value = address, onValueChange = { address = it }, placeholder = "Street address")
                EditProfileField(value = city, onValueChange = { city = it }, placeholder = "City")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EditProfileField(
                        value = stateField,
                        onValueChange = { stateField = it },
                        placeholder = "State",
                        modifier = Modifier.weight(0.4f),
                    )
                    EditProfileField(
                        value = zipCode,
                        onValueChange = { zipCode = it },
                        placeholder = "Zip",
                        modifier = Modifier.weight(0.6f),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Golf Handicap", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    EditProfileField(
                        value = handicapText,
                        onValueChange = { handicapText = it },
                        placeholder = "18",
                        modifier = Modifier.weight(0.45f),
                    )
                    parsedHandicap?.let {
                        Text(
                            text = handicapLabel(it),
                            color = LinksideColors.AccentLabel,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Text(
                    text = "Enter your USGA/WHS handicap index (e.g. 14.2). Scratch = 0.",
                    color = LinksideColors.TextTertiary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                title = if (isLoading) "Saving…" else "Save Changes",
                onClick = {
                    onSave(firstName, lastName, address, city, stateField, zipCode, handicapText)
                },
                enabled = canSave && !isLoading,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
