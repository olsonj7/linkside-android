package com.linkside.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLoginScreen(
    isLoading: Boolean,
    onSendCode: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
    title: String = "Enter your phone",
    subtitle: String = "We'll text you a code to sign in.",
) {
    var phone by rememberSaveable { mutableStateOf("") }
    val isValid = PhoneUtils.isValidPhone(phone)

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            TopAppBar(
                title = { Text("Sign In") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                actions = {
                    if (onSignOut != null) {
                        TextButton(onClick = onSignOut) {
                            Text("Sign out", color = LinksideColors.TextSecondary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LinksideColors.Primary,
                    titleContentColor = LinksideColors.TextPrimary,
                    navigationIconContentColor = LinksideColors.TextPrimary,
                    actionIconContentColor = LinksideColors.TextSecondary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = LinksideColors.TextPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = LinksideColors.TextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("(555) 555-5555") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            if (phone.isNotBlank() && !isValid) {
                Text(
                    text = "Enter a valid phone number (10 digits for US, or include + for international).",
                    style = MaterialTheme.typography.labelSmall,
                    color = LinksideColors.Danger,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                title = if (isLoading) "Sending…" else "Send Code",
                onClick = { onSendCode(phone) },
                enabled = isValid && !isLoading,
            )

            Text(
                text = "By tapping Send Code, you agree to receive a one-time SMS verification code from Linkside.",
                style = MaterialTheme.typography.labelSmall,
                color = LinksideColors.TextTertiary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
