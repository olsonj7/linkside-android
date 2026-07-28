package com.linkside.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun PrimaryButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = LinksideColors.Gold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LinksideColors.Gold,
            contentColor = LinksideColors.OnGold,
            disabledContainerColor = LinksideColors.Gold.copy(alpha = 0.45f),
            disabledContentColor = LinksideColors.OnGold.copy(alpha = 0.6f),
        ),
    ) {
        Row {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = title)
        }
    }
}

@Composable
fun SecondaryButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, LinksideColors.AccentLabel),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = LinksideColors.Card,
            contentColor = LinksideColors.AccentLabel,
        ),
    ) {
        Text(text = title)
    }
}

@Composable
fun AccentPrimaryButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LinksideColors.Accent,
            contentColor = LinksideColors.OnGold,
            disabledContainerColor = LinksideColors.Accent.copy(alpha = 0.45f),
            disabledContentColor = LinksideColors.OnGold.copy(alpha = 0.6f),
        ),
    ) {
        Row {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = title, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        }
    }
}

@Composable
fun DarkActionButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    // Matches iOS GoogleSignInButton: Theme.card + secondary stroke.
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, LinksideColors.TextSecondary.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = LinksideColors.Card,
            contentColor = LinksideColors.TextPrimary,
            disabledContainerColor = LinksideColors.Card.copy(alpha = 0.5f),
            disabledContentColor = LinksideColors.TextPrimary.copy(alpha = 0.5f),
        ),
    ) {
        Row {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = LinksideColors.TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = title)
        }
    }
}
