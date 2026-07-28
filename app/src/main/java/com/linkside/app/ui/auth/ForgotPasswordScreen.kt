package com.linkside.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    isLoading: Boolean,
    onBack: () -> Unit,
    onSendCode: (email: String, onSent: () -> Unit) -> Unit,
    onReset: (email: String, code: String, newPassword: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by rememberSaveable { mutableStateOf(0) } // 0 email, 1 code
    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    val emailOk = PhoneUtils.isValidEmail(email.trim())
    val canSend = !isLoading && emailOk
    val canReset = !isLoading &&
        emailOk &&
        code.trim().length >= 4 &&
        password.length >= 6 &&
        password == confirm

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            TopAppBar(
                title = { Text(if (step == 0) "Reset Password" else "Enter Code") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == 1) step = 0 else onBack()
                    }) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                if (step == 0) {
                    "Enter the email address linked to your account."
                } else {
                    "Check your email for a 6-digit code, then set a new password."
                },
                color = LinksideColors.TextSecondary,
            )
            if (step == 0) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                PrimaryButton(
                    title = if (isLoading) "Sending…" else "Send Reset Code",
                    onClick = {
                        onSendCode(email.trim()) { step = 1 }
                    },
                    enabled = canSend,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter { ch -> ch.isDigit() }.take(6) },
                    label = { Text("Reset code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "New password",
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
                    title = if (isLoading) "Saving…" else "Reset Password",
                    onClick = { onReset(email.trim(), code.trim(), password) },
                    enabled = canReset,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
