package com.linkside.app.ui.contest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linkside.app.data.model.ContestWin
import com.linkside.app.data.model.User
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors
import com.linkside.app.viewmodel.ContestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestClaimScreen(
    win: ContestWin,
    user: User,
    viewModel: ContestViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentWin = state.win ?: win

    var name by rememberSaveable {
        mutableStateOf(
            currentWin.shippingName
                ?: listOfNotNull(user.firstName, user.lastName).joinToString(" ").trim(),
        )
    }
    var email by rememberSaveable {
        mutableStateOf(currentWin.shippingEmail ?: user.email.orEmpty())
    }
    var address by rememberSaveable {
        mutableStateOf(currentWin.shippingAddress ?: user.address.orEmpty())
    }
    var city by rememberSaveable {
        mutableStateOf(currentWin.shippingCity ?: user.city.orEmpty())
    }
    var region by rememberSaveable {
        mutableStateOf(currentWin.shippingState ?: user.state.orEmpty())
    }
    var zip by rememberSaveable {
        mutableStateOf(currentWin.shippingZip ?: user.zipCode.orEmpty())
    }

    val canSubmit = name.trim().isNotEmpty() && address.trim().isNotEmpty() && !state.isClaiming
    val monthLabel = remember(currentWin.month) { formatContestMonth(currentWin.month) }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(
                title = "Claim Your Prize",
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = LinksideColors.Gold,
                modifier = Modifier.size(40.dp),
            )
            Text(
                "You won ${currentWin.prize}!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = LinksideColors.TextPrimary,
            )
            Text(
                "You brought ${currentWin.inviteCount} friends to Linkside in $monthLabel. Confirm where we should send your prize.",
                style = MaterialTheme.typography.bodyMedium,
                color = LinksideColors.TextSecondary,
            )

            if (currentWin.claimed) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LinksideColors.Accent.copy(alpha = 0.12f))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = LinksideColors.Accent,
                    )
                    Text(
                        if (currentWin.fulfilled) {
                            "Prize shipped!"
                        } else {
                            "Claim submitted — we'll be in touch to ship your prize."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = LinksideColors.TextPrimary,
                    )
                }
            } else {
                ClaimField(value = name, onValueChange = { name = it }, label = "Full Name")
                ClaimField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    keyboardType = KeyboardType.Email,
                )
                ClaimField(value = address, onValueChange = { address = it }, label = "Shipping Address")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ClaimField(
                        value = city,
                        onValueChange = { city = it },
                        label = "City",
                        modifier = Modifier.weight(1f),
                    )
                    ClaimField(
                        value = region,
                        onValueChange = { region = it },
                        label = "State",
                        modifier = Modifier.weight(0.45f),
                    )
                }
                ClaimField(
                    value = zip,
                    onValueChange = { zip = it },
                    label = "ZIP",
                    keyboardType = KeyboardType.Number,
                )

                state.claimError?.let {
                    Text(it, color = LinksideColors.Danger, style = MaterialTheme.typography.bodySmall)
                }

                PrimaryButton(
                    title = if (state.isClaiming) "Submitting…" else "Submit Claim",
                    onClick = {
                        viewModel.claimPrize(
                            month = currentWin.month,
                            name = name.trim(),
                            email = email.trim().takeIf { it.isNotEmpty() },
                            address = address.trim(),
                            city = city.trim().takeIf { it.isNotEmpty() },
                            state = region.trim().takeIf { it.isNotEmpty() },
                            zip = zip.trim().takeIf { it.isNotEmpty() },
                            onSuccess = onBack,
                        )
                    },
                    enabled = canSubmit,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ClaimField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = if (keyboardType == KeyboardType.Email) {
                KeyboardCapitalization.None
            } else {
                KeyboardCapitalization.Words
            },
        ),
    )
}

private fun formatContestMonth(month: String): String {
    val parts = month.split("-")
    if (parts.size != 2) return month
    val year = parts[0]
    val m = parts[1].toIntOrNull() ?: return month
    val names = listOf(
        "", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )
    if (m !in 1..12) return month
    return "${names[m]} $year"
}
