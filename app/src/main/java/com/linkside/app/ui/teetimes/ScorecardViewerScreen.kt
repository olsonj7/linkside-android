package com.linkside.app.ui.teetimes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.ScorecardHole
import com.linkside.app.data.model.TeeTimeScorecard
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.components.SecondaryButton
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorecardViewerScreen(
    courseName: String,
    scorecards: List<TeeTimeScorecard>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onShareRound: (() -> Unit)? = null,
    onPlayerOfTheDay: (() -> Unit)? = null,
) {
    val playable = remember(scorecards) {
        scorecards.filter { it.playerName != "_specs_" && it.source != null }
            .filter { card ->
                (card.total ?: 0) > 0 || card.holes.orEmpty().any { (it.score ?: 0) > 0 }
            }
    }
    var expandedId by remember { mutableStateOf<String?>(playable.firstOrNull()?.id) }

    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = {
            LinksideTopAppBar(title = "Scorecards", onBack = onBack)
        },
    ) { padding ->
        if (playable.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No scorecards yet", fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
                Text(
                    "When the host or teammates enter scores, they’ll show up here.",
                    color = LinksideColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LinksideColors.TextPrimary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }
                if (onShareRound != null || onPlayerOfTheDay != null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (onShareRound != null) {
                                PrimaryButton(title = "Share Round", onClick = onShareRound)
                            }
                            if (onPlayerOfTheDay != null) {
                                SecondaryButton(title = "Player of the Day", onClick = onPlayerOfTheDay)
                            }
                        }
                    }
                }
                items(playable, key = { it.id }) { card ->
                    val expanded = expandedId == card.id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LinksideColors.Card)
                            .clickable { expandedId = if (expanded) null else card.id }
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                card.playerName ?: "Player",
                                fontWeight = FontWeight.SemiBold,
                                color = LinksideColors.TextPrimary,
                            )
                            Text(
                                "Total ${cardTotal(card)}",
                                fontWeight = FontWeight.Bold,
                                color = LinksideColors.AccentLabel,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ScoreChip("F9", card.front9 ?: front9(card))
                            ScoreChip("B9", card.back9 ?: back9(card))
                            ScoreChip("Tot", cardTotal(card))
                        }
                        if (expanded) {
                            HoleGrid(holes = card.holes.orEmpty().sortedBy { it.hole })
                        } else {
                            Text(
                                "Tap to expand hole-by-hole",
                                style = MaterialTheme.typography.labelSmall,
                                color = LinksideColors.TextTertiary,
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ScoreChip(label: String, value: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(LinksideColors.Muted.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LinksideColors.TextSecondary)
        Text("$value", fontWeight = FontWeight.Bold, color = LinksideColors.TextPrimary)
    }
}

@Composable
private fun HoleGrid(holes: List<ScorecardHole>) {
    if (holes.isEmpty()) {
        Text("No hole details", color = LinksideColors.TextSecondary)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        holes.chunked(9).forEach { nine ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                nine.forEach { hole ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${hole.hole}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinksideColors.TextTertiary,
                        )
                        Text(
                            "${hole.score ?: "-"}",
                            fontWeight = FontWeight.SemiBold,
                            color = scoreColor(hole),
                        )
                        hole.par?.let {
                            Text("p$it", style = MaterialTheme.typography.labelSmall, color = LinksideColors.TextTertiary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun scoreColor(hole: ScorecardHole): androidx.compose.ui.graphics.Color {
    val score = hole.score ?: return LinksideColors.TextPrimary
    val par = hole.par ?: return LinksideColors.TextPrimary
    return when {
        score < par -> LinksideColors.AccentLabel
        score > par -> LinksideColors.Danger
        else -> LinksideColors.TextPrimary
    }
}

private fun cardTotal(card: TeeTimeScorecard): Int {
    val holeSum = card.holes?.mapNotNull { it.score }?.sum() ?: 0
    return card.total ?: holeSum
}

private fun front9(card: TeeTimeScorecard): Int =
    card.holes.orEmpty().filter { it.hole <= 9 }.mapNotNull { it.score }.sum()

private fun back9(card: TeeTimeScorecard): Int =
    card.holes.orEmpty().filter { it.hole > 9 }.mapNotNull { it.score }.sum()
