package com.linkside.app.ui.teetimes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.PlayFormat
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TeeTimeWindow
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private enum class EditTimeMode { Specific, Flexible }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTeeTimeScreen(
    teeTime: TeeTime,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: (
        date: Instant,
        golfersNeeded: Int,
        timeMode: String,
        timeWindows: List<String>,
        playFormat: String?,
        greenFee: Double?,
        holesCount: Int,
        roundName: String?,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val initialZoned = remember(teeTime.id) {
        teeTime.parsedInstant()?.atZone(zone)
    }
    var teeDate by remember(teeTime.id) {
        mutableStateOf(initialZoned?.toLocalDate() ?: LocalDate.now().plusDays(1))
    }
    var teeClock by remember(teeTime.id) {
        mutableStateOf(initialZoned?.toLocalTime() ?: LocalTime.of(12, 0))
    }
    var timeMode by remember(teeTime.id) {
        mutableStateOf(
            if (teeTime.isFlexibleTime) EditTimeMode.Flexible else EditTimeMode.Specific,
        )
    }
    var selectedWindows by remember(teeTime.id) {
        val windows = teeTime.timeWindows.mapNotNull { raw ->
            TeeTimeWindow.entries.firstOrNull { it.raw == raw }
        }.toSet()
        mutableStateOf(if (windows.isEmpty()) setOf(TeeTimeWindow.ANY) else windows)
    }
    var golfersNeeded by remember(teeTime.id) {
        mutableIntStateOf(teeTime.golfersNeeded.coerceIn(1, 20))
    }
    var holesCount by remember(teeTime.id) {
        mutableIntStateOf(teeTime.holesCount ?: 18)
    }
    var roundName by remember(teeTime.id) {
        mutableStateOf(teeTime.roundName.orEmpty())
    }
    var selectedFormat by remember(teeTime.id) {
        mutableStateOf(PlayFormat.entries.firstOrNull { it.raw == teeTime.playFormat })
    }
    var greenFeeText by remember(teeTime.id) {
        mutableStateOf(teeTime.greenFee?.let { fee ->
            if (fee % 1.0 == 0.0) fee.toInt().toString() else fee.toString()
        }.orEmpty())
    }
    var localError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val displayError = localError ?: errorMessage

    fun toggleWindow(window: TeeTimeWindow) {
        selectedWindows = when {
            window == TeeTimeWindow.ANY -> setOf(TeeTimeWindow.ANY)
            selectedWindows.contains(window) -> {
                val next = selectedWindows - window - TeeTimeWindow.ANY
                if (next.isEmpty()) setOf(TeeTimeWindow.ANY) else next
            }
            else -> (selectedWindows - TeeTimeWindow.ANY) + window
        }
    }

    fun saveDateInstant(): Instant {
        val local = if (timeMode == EditTimeMode.Specific) {
            teeDate.atTime(teeClock)
        } else {
            teeDate.atTime(12, 0)
        }
        return local.atZone(zone).toInstant()
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(
                title = "Edit Tee Time",
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                teeTime.courseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = LinksideColors.TextPrimary,
            )

            Text(
                "Round Name (optional)",
                color = LinksideColors.TextSecondary,
                style = MaterialTheme.typography.labelLarge,
            )
            OutlinedTextField(
                value = roundName,
                onValueChange = { if (it.length <= 60) roundName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Father's Day Round, Birthday Round") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LinksideColors.AccentLabel,
                    cursorColor = LinksideColors.AccentLabel,
                ),
            )

            Text("Date", color = LinksideColors.TextSecondary, style = MaterialTheme.typography.labelLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EditModeChip(
                    title = "Specific Time",
                    selected = timeMode == EditTimeMode.Specific,
                    onClick = { timeMode = EditTimeMode.Specific },
                    modifier = Modifier.weight(1f),
                )
                EditModeChip(
                    title = "Any / Time Window",
                    selected = timeMode == EditTimeMode.Flexible,
                    onClick = { timeMode = EditTimeMode.Flexible },
                    modifier = Modifier.weight(1f),
                )
            }

            EditPickerRow(
                label = "Date",
                value = dateFormatter.format(teeDate),
                expanded = showDatePicker,
                onClick = { showDatePicker = true },
            )

            if (timeMode == EditTimeMode.Specific) {
                EditPickerRow(
                    label = "Time",
                    value = timeFormatter.format(teeClock),
                    expanded = showTimePicker,
                    onClick = { showTimePicker = true },
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LinksideColors.Card)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Preferred Time Window",
                        color = LinksideColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TeeTimeWindow.entries.forEach { window ->
                            EditWindowChip(
                                label = window.label,
                                selected = selectedWindows.contains(window),
                                onClick = { toggleWindow(window) },
                            )
                        }
                    }
                }
            }

            Text("Group Size", color = LinksideColors.TextSecondary, style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LinksideColors.Card)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Golfers Needed", color = LinksideColors.TextPrimary, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { if (golfersNeeded > 1) golfersNeeded -= 1 },
                    enabled = golfersNeeded > 1,
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = if (golfersNeeded > 1) LinksideColors.AccentLabel else LinksideColors.Muted,
                    )
                }
                Text(
                    "$golfersNeeded",
                    fontWeight = FontWeight.Bold,
                    color = LinksideColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                IconButton(
                    onClick = { if (golfersNeeded < 20) golfersNeeded += 1 },
                    enabled = golfersNeeded < 20,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = if (golfersNeeded < 20) LinksideColors.AccentLabel else LinksideColors.Muted,
                    )
                }
            }

            Text("Holes", color = LinksideColors.TextSecondary, style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(9, 18).forEach { value ->
                    EditModeChip(
                        title = "$value Holes",
                        selected = holesCount == value,
                        onClick = { holesCount = value },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text(
                "Play Format (optional)",
                color = LinksideColors.TextSecondary,
                style = MaterialTheme.typography.labelLarge,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PlayFormat.entries.forEach { format ->
                    val selected = selectedFormat == format
                    Text(
                        text = format.displayName,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) LinksideColors.AccentLabel else LinksideColors.Muted)
                            .clickable {
                                selectedFormat = if (selected) null else format
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        color = if (selected) LinksideColors.OnGold else LinksideColors.TextPrimary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Text(
                "Green Fee incl. Cart (optional)",
                color = LinksideColors.TextSecondary,
                style = MaterialTheme.typography.labelLarge,
            )
            OutlinedTextField(
                value = greenFeeText,
                onValueChange = { greenFeeText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 65") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = LinksideColors.TextSecondary)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LinksideColors.TextPrimary,
                    unfocusedTextColor = LinksideColors.TextPrimary,
                    focusedBorderColor = LinksideColors.Accent,
                    unfocusedBorderColor = LinksideColors.Muted,
                    cursorColor = LinksideColors.Accent,
                    focusedContainerColor = LinksideColors.Card,
                    unfocusedContainerColor = LinksideColors.Card,
                ),
            )

            // Booking Link — auto-detected by Linkside, not editable
            Text(
                "Booking Link",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = LinksideColors.TextSecondary,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LinksideColors.Card)
                    .then(
                        if (!teeTime.bookingUrl.isNullOrBlank()) {
                            Modifier.clickable {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(teeTime.bookingUrl)),
                                    )
                                }
                            }
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = null,
                    tint = LinksideColors.TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                val url = teeTime.bookingUrl?.takeIf { it.isNotBlank() }
                if (url != null) {
                    Text(
                        text = url,
                        color = LinksideColors.AccentLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Text(
                        text = "Not found yet — Linkside automatically looks this up for you",
                        color = LinksideColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (displayError != null) {
                Text(displayError, color = LinksideColors.Danger, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                title = if (isLoading) "Saving…" else "Save Changes",
                enabled = !isLoading,
                onClick = {
                    val instant = saveDateInstant()
                    if (instant.isBefore(Instant.now())) {
                        localError = "Tee time must be in the future."
                        return@PrimaryButton
                    }
                    localError = null
                    onSave(
                        instant,
                        golfersNeeded,
                        if (timeMode == EditTimeMode.Flexible) "flexible" else "specific",
                        if (timeMode == EditTimeMode.Flexible) {
                            selectedWindows.map { it.raw }
                        } else {
                            emptyList()
                        },
                        selectedFormat?.raw,
                        greenFeeText.toDoubleOrNull(),
                        holesCount,
                        roundName.trim().take(60),
                    )
                },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        // Material DatePicker works in UTC (millis = UTC midnight of the picked day),
        // so seed and read the value in UTC to avoid an off-by-one date shift.
        val state = rememberDatePickerState(
            initialSelectedDateMillis = teeDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            teeDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) { Text("OK", color = LinksideColors.AccentLabel) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = LinksideColors.TextSecondary)
                }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = teeClock.hour,
            initialMinute = teeClock.minute,
            is24Hour = false,
        )
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        teeClock = LocalTime.of(state.hour, state.minute)
                        showTimePicker = false
                    },
                ) { Text("OK", color = LinksideColors.AccentLabel) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = LinksideColors.TextSecondary)
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimePicker(state = state)
            }
        }
    }
}

@Composable
private fun EditModeChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) LinksideColors.AccentLabel else LinksideColors.Muted)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        color = if (selected) LinksideColors.OnGold else LinksideColors.TextPrimary,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun EditPickerRow(
    label: String,
    value: String,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Muted)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = LinksideColors.TextSecondary,
            modifier = Modifier
                .padding(start = 4.dp)
                .rotate(if (expanded) 180f else 0f),
        )
    }
}

@Composable
private fun EditWindowChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) LinksideColors.AccentLabel else LinksideColors.Muted)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        color = if (selected) LinksideColors.OnGold else LinksideColors.TextPrimary,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        style = MaterialTheme.typography.bodySmall,
    )
}
