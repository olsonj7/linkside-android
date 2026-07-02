package com.linkside.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.components.SecondaryButton
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun WelcomeScreen(
    onPhoneClick: () -> Unit,
    onEmailClick: () -> Unit,
    onGoogleClick: () -> Unit,
    isGoogleLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Linkside",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Fill your tee time fast",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Invite golf buddies by text and track who's in.",
            style = MaterialTheme.typography.bodyLarge,
            color = LinksideColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(title = "Continue with Phone", onClick = onPhoneClick)
        Text(
            text = "Recommended — get instant SMS invites & RSVPs.",
            style = MaterialTheme.typography.labelSmall,
            color = LinksideColors.Accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )

        SecondaryButton(title = "Continue with Email", onClick = onEmailClick)

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryButton(
            title = if (isGoogleLoading) "Signing in…" else "Continue with Google",
            onClick = onGoogleClick,
            enabled = !isGoogleLoading,
        )

        Text(
            text = "Simple golf scheduling, built for speed.",
            style = MaterialTheme.typography.bodySmall,
            color = LinksideColors.TextSecondary,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
