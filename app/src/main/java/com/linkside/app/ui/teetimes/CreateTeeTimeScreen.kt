package com.linkside.app.ui.teetimes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.Friend
import com.linkside.app.data.model.FriendGroup
import com.linkside.app.data.model.GolfCourse
import com.linkside.app.data.model.PlayFormat
import com.linkside.app.data.model.TeeTimeWindow
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class TimeMode { Specific, Flexible }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeeTimeScreen(
    savedGolfers: List<Friend>,
    friendGroups: List<FriendGroup>,
    defaultGroupSize: Int,
    courseResults: List<GolfCourse>,
    isSearching: Boolean,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSearchCourses: (String) -> Unit,
    onClearCourseSearch: () -> Unit = {},
    onCreate: (
        courseName: String,
        courseId: String?,
        date: Instant,
        golfersNeeded: Int,
        invites: List<Friend>,
        timeMode: String,
        timeWindows: List<String>,
        playFormat: String?,
        greenFee: Double?,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    var courseQuery by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf<GolfCourse?>(null) }
    var teeDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var teeTime by remember { mutableStateOf(LocalTime.of(18, 30)) }
    var timeMode by remember { mutableStateOf(TimeMode.Specific) }
    var selectedWindows by remember { mutableStateOf(setOf(TeeTimeWindow.ANY)) }
    var golfersNeeded by remember { mutableIntStateOf(defaultGroupSize.coerceIn(2, 4)) }
    var selectedPhones by remember { mutableStateOf(setOf<String>()) }
    var selectedFormat by remember { mutableStateOf<PlayFormat?>(null) }
    var greenFeeText by remember { mutableStateOf("") }
    var showFriendPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onClearCourseSearch()
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val showCourseResults = selectedCourse == null &&
        courseQuery.trim().length >= 2 &&
        courseResults.isNotEmpty()

    val selectedFriends = remember(savedGolfers, friendGroups, selectedPhones) {
        val seen = mutableSetOf<String>()
        val combined = mutableListOf<Friend>()
        (friendGroups.flatMap { it.members } + savedGolfers).forEach { friend ->
            if (seen.add(friend.phone)) combined.add(friend)
        }
        combined.filter { it.phone in selectedPhones }
    }

    val canSend = selectedCourse != null && selectedPhones.isNotEmpty() && !isLoading

    if (showFriendPicker) {
        InviteGolfersSheet(
            groups = friendGroups,
            savedGolfers = savedGolfers,
            selectedPhones = selectedPhones,
            onSelectionChange = { selectedPhones = it },
            onDismiss = { showFriendPicker = false },
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = teeDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            teeDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Done", color = LinksideColors.AccentLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = LinksideColors.TextSecondary)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = teeTime.hour,
            initialMinute = teeTime.minute,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        teeTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) {
                    Text("Done", color = LinksideColors.AccentLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = LinksideColors.TextSecondary)
                }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = { LinksideTopAppBar(onBack = onBack) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create Tee Time",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = LinksideColors.TextPrimary,
                )
            }

            item {
                OutlinedTextField(
                    value = courseQuery,
                    onValueChange = {
                        courseQuery = it
                        selectedCourse = null
                        val trimmed = it.trim()
                        if (trimmed.length >= 2) {
                            onSearchCourses(trimmed)
                        } else {
                            onClearCourseSearch()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search golf course", color = LinksideColors.TextTertiary) },
                    trailingIcon = {
                        if (courseQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    courseQuery = ""
                                    selectedCourse = null
                                    onClearCourseSearch()
                                },
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = LinksideColors.TextSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LinksideColors.Card,
                        unfocusedContainerColor = LinksideColors.Card,
                        focusedBorderColor = LinksideColors.Muted,
                        unfocusedBorderColor = LinksideColors.Muted,
                        focusedTextColor = LinksideColors.TextPrimary,
                        unfocusedTextColor = LinksideColors.TextPrimary,
                    ),
                )
                if (isSearching) {
                    Text(
                        "Searching…",
                        color = LinksideColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (showCourseResults) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(LinksideColors.Card),
                    ) {
                        courseResults.take(5).forEachIndexed { index, course ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCourse = course
                                        courseQuery = course.name
                                        onClearCourseSearch()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                            ) {
                                Text(course.name, fontWeight = FontWeight.Medium, color = LinksideColors.TextPrimary)
                                course.address?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = LinksideColors.TextSecondary)
                                }
                            }
                            if (index < courseResults.take(5).lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(LinksideColors.Muted),
                                )
                            }
                        }
                    }
                }
            }

            selectedCourse?.let { course ->
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Selected course:", color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Text(course.name, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                    }
                }
            }

            item {
                Text(
                    text = "Time Preference",
                    fontWeight = FontWeight.Medium,
                    color = LinksideColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimeModeChip(
                        title = "Specific Time",
                        selected = timeMode == TimeMode.Specific,
                        modifier = Modifier.weight(1f),
                        onClick = { timeMode = TimeMode.Specific },
                    )
                    TimeModeChip(
                        title = "Any / Time Window",
                        selected = timeMode == TimeMode.Flexible,
                        modifier = Modifier.weight(1f),
                        onClick = { timeMode = TimeMode.Flexible },
                    )
                }
            }

            item {
                PickerRow(
                    label = "Date",
                    value = teeDate.format(dateFormatter),
                    expanded = showDatePicker,
                    onClick = {
                        showDatePicker = true
                        showTimePicker = false
                    },
                )
            }

            if (timeMode == TimeMode.Specific) {
                item {
                    PickerRow(
                        label = "Time",
                        value = teeTime.format(timeFormatter),
                        expanded = showTimePicker,
                        onClick = {
                            showTimePicker = true
                            showDatePicker = false
                        },
                    )
                }
            } else {
                item {
                    Text(
                        text = "Preferred Time Window",
                        fontWeight = FontWeight.Medium,
                        color = LinksideColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(TeeTimeWindow.ANY, TeeTimeWindow.MORNING, TeeTimeWindow.MIDDAY).forEach { window ->
                                TimeWindowChip(
                                    window = window,
                                    selected = window in selectedWindows,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selectedWindows = if (window in selectedWindows) {
                                            selectedWindows - window
                                        } else {
                                            selectedWindows + window
                                        }.ifEmpty { setOf(TeeTimeWindow.ANY) }
                                    },
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(TeeTimeWindow.AFTERNOON, TeeTimeWindow.TWILIGHT).forEach { window ->
                                TimeWindowChip(
                                    window = window,
                                    selected = window in selectedWindows,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selectedWindows = if (window in selectedWindows) {
                                            selectedWindows - window
                                        } else {
                                            selectedWindows + window
                                        }.ifEmpty { setOf(TeeTimeWindow.ANY) }
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Text(
                        text = "Invitees can reply before a specific tee time is booked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LinksideColors.TextSecondary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            item {
                Text(
                    text = "Group size (including you)",
                    fontWeight = FontWeight.Medium,
                    color = LinksideColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    (2..4).forEach { value ->
                        GroupSizeChip(
                            value = value,
                            selected = golfersNeeded == value,
                            modifier = Modifier.weight(1f),
                            onClick = { golfersNeeded = value },
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Golfers",
                    fontWeight = FontWeight.Medium,
                    color = LinksideColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LinksideColors.Card)
                        .clickable { showFriendPicker = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Invite golfers", color = LinksideColors.TextPrimary, modifier = Modifier.weight(1f))
                    Text(
                        text = if (selectedPhones.isEmpty()) "None" else "${selectedPhones.size} selected",
                        color = LinksideColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LinksideColors.TextSecondary,
                    )
                }
                if (selectedFriends.isNotEmpty()) {
                    Text(
                        text = selectedFriends.joinToString(", ") { it.fullName },
                        style = MaterialTheme.typography.bodySmall,
                        color = LinksideColors.TextSecondary,
                        modifier = Modifier.padding(top = 6.dp),
                        maxLines = 2,
                    )
                }
            }

            item {
                Text(
                    text = "Play Format (optional)",
                    fontWeight = FontWeight.Medium,
                    color = LinksideColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PlayFormat.entries.forEach { format ->
                        FormatChip(
                            label = format.displayName,
                            icon = format.icon,
                            selected = selectedFormat == format,
                            onClick = {
                                selectedFormat = if (selectedFormat == format) null else format
                            },
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Green Fee incl. Cart (optional)",
                    fontWeight = FontWeight.Medium,
                    color = LinksideColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = greenFeeText,
                    onValueChange = { greenFeeText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 65", color = LinksideColors.TextTertiary) },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = LinksideColors.TextSecondary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LinksideColors.Card,
                        unfocusedContainerColor = LinksideColors.Card,
                        focusedBorderColor = LinksideColors.Muted,
                        unfocusedBorderColor = LinksideColors.Muted,
                        focusedTextColor = LinksideColors.TextPrimary,
                        unfocusedTextColor = LinksideColors.TextPrimary,
                    ),
                )
            }

            item {
                val instant = teeDate.atTime(teeTime).atZone(ZoneId.systemDefault()).toInstant()
                val greenFee = greenFeeText.trim().toDoubleOrNull()
                val apiTimeMode = if (timeMode == TimeMode.Specific) "specific" else "flexible"
                val windows = if (timeMode == TimeMode.Flexible) {
                    selectedWindows.map { it.raw }
                } else {
                    emptyList()
                }

                PrimaryButton(
                    title = if (isLoading) "Sending…" else "Send Invites",
                    onClick = {
                        val course = selectedCourse ?: return@PrimaryButton
                        onCreate(
                            course.name,
                            course.placeId,
                            instant,
                            golfersNeeded,
                            selectedFriends,
                            apiTimeMode,
                            windows,
                            selectedFormat?.raw,
                            greenFee,
                        )
                    },
                    enabled = canSend,
                )
                Text(
                    text = "Your tee time will be saved. Friends who aren't on Linkside yet will receive a personal text from you — you'll send each one individually.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LinksideColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TimeModeChip(
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
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@Composable
private fun PickerRow(
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
private fun GroupSizeChip(
    value: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "$value",
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) LinksideColors.AccentLabel else LinksideColors.Muted)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        color = if (selected) LinksideColors.OnGold else LinksideColors.TextPrimary,
        fontWeight = FontWeight.SemiBold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@Composable
private fun TimeWindowChip(
    window: TeeTimeWindow,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = window.label,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) LinksideColors.AccentLabel else LinksideColors.Muted)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        color = if (selected) LinksideColors.OnGold else LinksideColors.TextPrimary,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        style = MaterialTheme.typography.bodySmall,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@Composable
private fun FormatChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) LinksideColors.AccentLabel else LinksideColors.Muted)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (selected) LinksideColors.OnGold else LinksideColors.TextPrimary,
        )
        Text(
            text = label,
            color = if (selected) LinksideColors.OnGold else LinksideColors.TextPrimary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private val PlayFormat.icon: ImageVector
    get() = when (this) {
        PlayFormat.STROKE_PLAY -> Icons.Default.ListAlt
        PlayFormat.SCRAMBLE -> Icons.Default.Groups
        PlayFormat.BEST_BALL -> Icons.Default.Star
    }
