package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinksideTopAppBar(
    onBack: (() -> Unit)? = null,
    title: String? = null,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LinksideColors.Muted),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LinksideColors.TextPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        },
        title = {
            if (title != null) {
                Text(title, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
            } else {
                LinksideWordmark(fontSize = 20)
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LinksideColors.Primary,
            titleContentColor = LinksideColors.TextPrimary,
            navigationIconContentColor = LinksideColors.TextPrimary,
            actionIconContentColor = LinksideColors.AccentLabel,
        ),
    )
}
