package com.linkside.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkside.app.ui.components.DarkActionButton
import com.linkside.app.ui.components.LinksideWordmark
import com.linkside.app.ui.components.PrimaryButton
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
            .background(LinksideColors.Primary)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        LinksideWordmark(
            fontSize = 42,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Fill your tee time fast",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = LinksideColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Invite golf buddies by text and track who's in.",
            fontSize = 16.sp,
            color = LinksideColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 12.dp, end = 12.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryButton(title = "Continue with Phone", onClick = onPhoneClick)
            Text(
                text = "Recommended — get instant SMS invites & RSVPs.",
                fontSize = 11.sp,
                color = LinksideColors.AccentLabel,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )

            PrimaryButton(
                title = "Continue with Email",
                onClick = onEmailClick,
                modifier = Modifier.alpha(0.85f),
            )

            DarkActionButton(
                title = if (isGoogleLoading) "Signing in…" else "Continue with Google",
                onClick = onGoogleClick,
                enabled = !isGoogleLoading,
            )
        }

        Text(
            text = "Simple golf scheduling, built for speed.",
            fontSize = 13.sp,
            color = LinksideColors.TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        )
    }
}
