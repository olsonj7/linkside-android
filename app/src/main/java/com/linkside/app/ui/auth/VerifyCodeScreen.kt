package com.linkside.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors

@Composable
fun VerifyCodeScreen(
    phone: String,
    isLoading: Boolean,
    onVerify: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var code by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Enter code",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Enter the 6-digit code we texted to $phone.",
            style = MaterialTheme.typography.bodyLarge,
            color = LinksideColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 8) code = it.filter { ch -> ch.isDigit() } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("123456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            title = if (isLoading) "Verifying…" else "Verify",
            onClick = { onVerify(code) },
            enabled = code.isNotBlank() && !isLoading,
        )
    }
}
