package com.linkside.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linkside.app.ui.components.LinksideWordmark
import com.linkside.app.ui.theme.LinksideColors

private val taglines = listOf(
    "Round up the GOATs",
    "Fill your tee time fast",
    "Golf is better with your people",
    "Tee times don't fill themselves",
)

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LinksideColors.Primary)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LinksideWordmark(
            fontSize = 36,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = taglines.random(),
            color = LinksideColors.TextSecondary,
            modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
        )
        CircularProgressIndicator(color = LinksideColors.Accent)
    }
}
