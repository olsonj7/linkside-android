package com.linkside.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LinksideColors.Card)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = LinksideColors.AccentLabel, modifier = Modifier.size(20.dp))
        Text(label, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Medium,
            color = LinksideColors.TextPrimary,
        )
    }
}

@Composable
fun ProfilePlaceholderRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LinksideColors.Card.copy(alpha = 0.85f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = LinksideColors.TextSecondary, modifier = Modifier.size(20.dp))
        Text(text, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = LinksideColors.TextTertiary)
    }
}

@Composable
fun SignInMethodCard(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    showCheck: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = LinksideColors.AccentLabel, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
            subtitle?.let {
                Text(it, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (showCheck) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LinksideColors.Success)
        }
    }
}

@Composable
fun FavoriteCourseRow(
    name: String,
    address: String?,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LinksideColors.Card)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Star, contentDescription = null, tint = LinksideColors.AccentLabel, modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium, color = LinksideColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            address?.let {
                Text(it, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = LinksideColors.TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ProfileAddChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(LinksideColors.Accent.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = LinksideColors.AccentLabel,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
fun SettingsStepperRow(
    label: String,
    icon: ImageVector,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LinksideColors.Card)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = LinksideColors.TextPrimary, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            color = LinksideColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = LinksideColors.TextPrimary)
            }
            Text(
                text = value.toString(),
                color = LinksideColors.AccentLabel,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            IconButton(onClick = onIncrement) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = LinksideColors.TextPrimary)
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LinksideColors.Card)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = LinksideColors.TextPrimary, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            color = LinksideColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LinksideColors.TextPrimary,
                checkedTrackColor = LinksideColors.Accent,
                uncheckedThumbColor = LinksideColors.TextSecondary,
                uncheckedTrackColor = LinksideColors.Muted,
            ),
        )
    }
}

@Composable
fun ProfileDestructiveRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = LinksideColors.Danger)
        Text(label, color = LinksideColors.Danger, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EditProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = LinksideColors.TextTertiary) },
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LinksideColors.Card,
            unfocusedContainerColor = LinksideColors.Card,
            focusedBorderColor = LinksideColors.Accent.copy(alpha = 0.4f),
            unfocusedBorderColor = LinksideColors.Muted,
            focusedTextColor = LinksideColors.TextPrimary,
            unfocusedTextColor = LinksideColors.TextPrimary,
        ),
    )
}

@Composable
fun EditProfileCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(LinksideColors.Accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

fun formatHomeAddress(
    address: String?,
    city: String?,
    state: String?,
    zipCode: String?,
): String? {
    val parts = mutableListOf<String>()
    address?.trim()?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
    val cityLine = buildString {
        city?.trim()?.takeIf { it.isNotEmpty() }?.let { append(it) }
        state?.trim()?.takeIf { it.isNotEmpty() }?.let {
            if (isNotEmpty()) append(", ")
            append(it)
        }
        zipCode?.trim()?.takeIf { it.isNotEmpty() }?.let {
            if (isNotEmpty()) append(' ')
            append(it)
        }
    }
    if (cityLine.isNotEmpty()) parts.add(cityLine)
    return parts.takeIf { it.isNotEmpty() }?.joinToString("\n")
}

fun handicapLabel(handicap: Double): String {
    if (handicap == 0.0) return "Scratch"
    if (handicap < 0) return "+${kotlin.math.abs(handicap).toInt()}"
    return if (handicap % 1.0 == 0.0) handicap.toInt().toString() else String.format("%.1f", handicap)
}

enum class PrimaryAuthMethod {
    APPLE, GOOGLE, EMAIL, PHONE,
}

fun primaryAuthMethod(user: com.linkside.app.data.model.User): PrimaryAuthMethod = when {
    !user.appleId.isNullOrBlank() -> PrimaryAuthMethod.APPLE
    !user.googleId.isNullOrBlank() -> PrimaryAuthMethod.GOOGLE
    !user.email.isNullOrBlank() -> PrimaryAuthMethod.EMAIL
    else -> PrimaryAuthMethod.PHONE
}
