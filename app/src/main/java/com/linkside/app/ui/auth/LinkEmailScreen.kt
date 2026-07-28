package com.linkside.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.modifier
import androidx.compose.ui.unit.dp
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkEmailScreen(
    isLoading: Boolean,
    onBack: () -> Unit,
    onLink: (email: String, password: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    val emailOk = PhoneUtils.isValidEmail(email.trim())
    val canSubmit = !isLoading &&
        emailOk &&
        password.length >= 6 &&
        password == confirm

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            TopAppBar(
                title = { Text("Link Email & Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LinksideColors.Primary,
                    titleContentColor = LinksideColors.TextPrimary,
                    navigationIconContentColor = LinksideColors.AccentLabel,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "You’ll be able to sign in with this email and password in addition to your other methods.",
                color = LinksideColors.TextSecondary,
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password (6+ characters)",
            )
            PasswordTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = "Confirm password",
            )
            if (password.isNotEmpty() && confirm.isNotEmpty() && password != confirm) {
                Text("Passwords don’t match", color = LinksideColors.Danger)
            }
            PrimaryButton(
                title = if (isLoading) "Saving…" else "Save Email & Password",
                onClick = { onLink(email.trim(), password) },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
