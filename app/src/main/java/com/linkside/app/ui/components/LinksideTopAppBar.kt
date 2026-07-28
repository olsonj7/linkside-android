package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var lastBackClickAt by remember { mutableLongStateOf(0L) }
    CenterAlignedTopAppBar(
        modifier = modifier,
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = {
                        val now = android.os.SystemClock.elapsedRealtime()
                        // Ignore a second tap in the same spot during/after the pop transition.
                        if (now - lastBackClickAt < 500L) return@IconButton
                        lastBackClickAt = now
                        onBack()
                    },
                ) {
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
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            } else {
                LinksideWordmark(
                    fontSize = 20,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = LinksideColors.Primary,
            titleContentColor = LinksideColors.TextPrimary,
            navigationIconContentColor = LinksideColors.TextPrimary,
            actionIconContentColor = LinksideColors.AccentLabel,
        ),
    )
}
