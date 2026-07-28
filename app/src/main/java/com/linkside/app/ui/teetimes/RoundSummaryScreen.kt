package com.linkside.app.ui.teetimes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.RoundSummary
import com.linkside.app.data.model.ScorecardShareEntry
import com.linkside.app.data.model.TeeTime
import com.linkside.app.data.model.TeeTimeScorecard
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.share.ScorecardShareContent
import com.linkside.app.ui.share.ShareLinks
import com.linkside.app.ui.share.cacheShareBitmap
import com.linkside.app.ui.share.copyTextToClipboard
import com.linkside.app.ui.share.rememberShareCardCapture
import com.linkside.app.ui.share.shareImageAndText
import com.linkside.app.ui.share.shareToInstagramStory
import com.linkside.app.ui.theme.LinksideColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundSummaryScreen(
    teeTime: TeeTime,
    summary: RoundSummary?,
    scorecards: List<TeeTimeScorecard>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onOpenPlayerOfTheDay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val players = remember(scorecards) { ScorecardShareEntry.rankedFrom(scorecards) }
    val shareText = remember(summary) {
        summary?.blurb ?: "Great round of golf!"
    }
    val capture = rememberShareCardCapture()
    var isSharing by remember { mutableStateOf(false) }

    LaunchedEffect(teeTime.id) { onLoad() }

    fun renderThen(block: (android.net.Uri) -> Unit) {
        if (isSharing) return
        isSharing = true
        scope.launch {
            try {
                val bitmap = capture.captureBitmap()
                val uri = cacheShareBitmap(context, bitmap)
                block(uri)
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "Couldn't prepare share image", Toast.LENGTH_SHORT).show()
            } finally {
                isSharing = false
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(title = "Your Round", onBack = onBack)
        },
    ) { padding ->
        when {
            isLoading && summary == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = LinksideColors.Accent)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    ScorecardShareContent(
                        teeTime = teeTime,
                        players = players,
                        recap = summary?.blurb,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .then(capture.layerModifier),
                    )

                    if (summary?.blurb != null) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(LinksideColors.Card)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(summary.blurb, color = LinksideColors.TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = {
                                copyTextToClipboard(context, "Round caption", shareText)
                                Toast.makeText(context, "Caption copied", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Copy caption", color = LinksideColors.AccentLabel)
                            }
                        }
                    }

                    if (errorMessage != null && summary == null) {
                        Text(
                            errorMessage,
                            color = LinksideColors.Danger,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SocialShareButton(
                            title = if (isSharing) "Preparing…" else "Share to Instagram",
                            icon = Icons.Default.CameraAlt,
                            gradient = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045)),
                            enabled = !isSharing,
                            onClick = {
                                renderThen { uri ->
                                    if (!shareToInstagramStory(context, uri)) {
                                        Toast.makeText(
                                            context,
                                            "Install Instagram to share to your story, or use More sharing options.",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    }
                                }
                            },
                        )
                        SocialShareButton(
                            title = if (isSharing) "Preparing…" else "Share to Facebook",
                            icon = Icons.Default.Facebook,
                            gradient = listOf(Color(0xFF1877F2), Color(0xFF0A5DC2)),
                            enabled = !isSharing,
                            onClick = {
                                renderThen { uri -> shareImageAndText(context, uri, shareText) }
                            },
                        )
                        PrimaryButton(
                            title = if (isSharing) "Preparing…" else "More sharing options",
                            icon = Icons.AutoMirrored.Filled.OpenInNew,
                            enabled = !isSharing,
                            onClick = {
                                renderThen { uri -> shareImageAndText(context, uri, shareText) }
                            },
                        )
                    }

                    if (players.isNotEmpty()) {
                        TextButton(
                            onClick = onOpenPlayerOfTheDay,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LinksideColors.Gold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Make a Player of the Day card", color = LinksideColors.Gold, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text("Done", color = LinksideColors.TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SocialShareButton(
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.6f),
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradient), RoundedCornerShape(14.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(icon, contentDescription = null)
                Text(title, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
