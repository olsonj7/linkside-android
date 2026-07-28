package com.linkside.app.ui.teetimes

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.ScorecardShareEntry
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TeeTimeScorecard
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.share.PlayerOfTheDayContent
import com.linkside.app.ui.share.ShareLinks
import com.linkside.app.ui.share.cacheShareBitmap
import com.linkside.app.ui.share.rememberShareCardCapture
import com.linkside.app.ui.share.shareImageAndText
import com.linkside.app.ui.theme.LinksideColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerOfTheDayScreen(
    teeTime: TeeTime,
    scorecards: List<TeeTimeScorecard>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val winner = remember(scorecards) { ScorecardShareEntry.rankedFrom(scorecards).firstOrNull() }
    val shareText = remember(winner, teeTime) {
        winner?.let {
            val thru = if (!it.isComplete) " (thru ${it.holesPlayed})" else ""
            "🏆 ${it.name} is Player of the Day at ${teeTime.courseName} with a ${it.total}$thru! Tracked on Linkside."
        } ?: "Player of the Day — tracked on Linkside."
    }
    val capture = rememberShareCardCapture()
    var isSharing by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(title = "Player of the Day", onBack = onBack)
        },
    ) { padding ->
        if (winner == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No scores recorded yet.", color = LinksideColors.TextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PlayerOfTheDayContent(
                    teeTime = teeTime,
                    winner = winner,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .then(capture.layerModifier),
                )

                PrimaryButton(
                    title = if (isSharing) "Preparing…" else "Share Player of the Day",
                    enabled = !isSharing,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onClick = {
                        if (isSharing) return@PrimaryButton
                        isSharing = true
                        scope.launch {
                            try {
                                val bitmap = capture.captureBitmap()
                                val uri = cacheShareBitmap(context, bitmap, "player_of_the_day.png")
                                shareImageAndText(context, uri, shareText)
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: "Couldn't prepare share image", Toast.LENGTH_SHORT).show()
                            } finally {
                                isSharing = false
                            }
                        }
                    },
                )

                TextButton(onClick = onBack) {
                    Text("Done", color = LinksideColors.TextSecondary)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
