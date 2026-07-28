package com.linkside.app.ui.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.linkside.app.data.api.PhoneUtils
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailAuthScreen(
    isLoading: Boolean,
    onBack: () -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
    onRegister: (
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        smsConsent: Boolean,
        phoneCode: String,
    ) -> Unit,
    onSendPhoneCode: (phone: String, onComplete: (success: Boolean) -> Unit) -> Unit,
    onForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isRegistering by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var phoneCode by rememberSaveable { mutableStateOf("") }
    var isCodeSent by rememberSaveable { mutableStateOf(false) }
    var isSendingCode by rememberSaveable { mutableStateOf(false) }
    var smsConsent by rememberSaveable { mutableStateOf(true) }

    val trimmedEmail = email.trim()
    val isEmailValid = PhoneUtils.isValidEmail(trimmedEmail)
    val isPasswordValid = password.length >= 6
    val phoneValid = PhoneUtils.isValidPhone(phone)
    val canSubmit = when {
        isLoading -> false
        isRegistering -> {
            isEmailValid &&
                isPasswordValid &&
                firstName.trim().isNotEmpty() &&
                lastName.trim().isNotEmpty() &&
                phoneValid &&
                isCodeSent &&
                phoneCode.trim().isNotEmpty()
        }
        else -> isEmailValid && isPasswordValid
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isRegistering) "Create Account" else "Email Sign In")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = LinksideColors.Primary,
                    titleContentColor = LinksideColors.TextPrimary,
                    navigationIconContentColor = LinksideColors.TextPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (isRegistering) "Create Account" else "Sign In",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = LinksideColors.TextPrimary,
            )
            Text(
                text = if (isRegistering) {
                    "Enter your details to get started."
                } else {
                    "Welcome back! Sign in with your email and password."
                },
                color = LinksideColors.TextSecondary,
            )

            if (isRegistering) {
                AuthField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "First name",
                    keyboardCapitalization = KeyboardCapitalization.Words,
                )
                AuthField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Last name",
                    keyboardCapitalization = KeyboardCapitalization.Words,
                )
            }

            AuthField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
            )
            if (trimmedEmail.isNotEmpty() && !isEmailValid) {
                Text("Enter a valid email address.", color = LinksideColors.Danger, style = MaterialTheme.typography.labelSmall)
            }

            if (isRegistering) {
                AuthField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        isCodeSent = false
                        phoneCode = ""
                    },
                    label = "Phone",
                    placeholder = "(555) 555-5555",
                    keyboardType = KeyboardType.Phone,
                )
                if (phone.isNotBlank() && !phoneValid) {
                    Text(
                        "Enter a valid phone number.",
                        color = LinksideColors.Danger,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (phoneValid) {
                    if (!isCodeSent) {
                        TextButton(
                            onClick = {
                                isSendingCode = true
                                onSendPhoneCode(phone) { ok ->
                                    if (ok) isCodeSent = true
                                    isSendingCode = false
                                }
                            },
                            enabled = !isSendingCode && !isLoading,
                        ) {
                            Text(
                                if (isSendingCode) "Sending code…" else "Send verification code",
                                color = LinksideColors.AccentLabel,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    } else {
                        AuthField(
                            value = phoneCode,
                            onValueChange = { phoneCode = it.filter { ch -> ch.isDigit() }.take(6) },
                            label = "Verification code",
                            keyboardType = KeyboardType.Number,
                        )
                        TextButton(
                            onClick = {
                                isSendingCode = true
                                onSendPhoneCode(phone) { _ ->
                                    isSendingCode = false
                                }
                            },
                            enabled = !isSendingCode && !isLoading,
                        ) {
                            Text(
                                if (isSendingCode) "Sending…" else "Didn't get a code? Resend",
                                color = LinksideColors.TextSecondary,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
                RowWithSwitch(
                    checked = smsConsent,
                    onCheckedChange = { smsConsent = it },
                    label = "Send me SMS notifications for invites and RSVPs.",
                )
            }

            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
            )
            if (isRegistering) {
                Text(
                    "Password must be at least 6 characters.",
                    color = LinksideColors.TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            PrimaryButton(
                title = when {
                    isLoading && isRegistering -> "Creating…"
                    isLoading -> "Signing In…"
                    isRegistering -> "Create Account"
                    else -> "Sign In"
                },
                onClick = {
                    if (isRegistering) {
                        onRegister(
                            trimmedEmail,
                            password,
                            firstName.trim(),
                            lastName.trim(),
                            phone,
                            smsConsent,
                            phoneCode.trim(),
                        )
                    } else {
                        onSignIn(trimmedEmail, password)
                    }
                },
                enabled = canSubmit,
            )

            if (!isRegistering) {
                TextButton(onClick = onForgotPassword) {
                    Text("Forgot password?", color = LinksideColors.Accent)
                }
            }

            TextButton(
                onClick = {
                    isRegistering = !isRegistering
                    isCodeSent = false
                    phoneCode = ""
                },
            ) {
                Text(
                    text = if (isRegistering) {
                        "Already have an account? Sign In"
                    } else {
                        "Don't have an account? Create Account"
                    },
                    color = LinksideColors.Accent,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = label,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = keyboardCapitalization,
        ),
    )
}

@Composable
private fun RowWithSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LinksideColors.TextSecondary,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
