package com.linkside.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun OnboardingScreen(
    isLoading: Boolean,
    onContinue: (firstName: String, lastName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    val canContinue = firstName.trim().isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "What should we\ncall you?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "We'll use your name to personalize invites.",
            style = MaterialTheme.typography.bodyLarge,
            color = LinksideColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("First name") },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Last name (optional)") },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            title = if (isLoading) "Loading…" else "Let's go →",
            onClick = { onContinue(firstName.trim(), lastName.trim()) },
            enabled = canContinue && !isLoading,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}
