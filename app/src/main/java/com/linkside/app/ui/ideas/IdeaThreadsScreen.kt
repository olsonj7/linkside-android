package com.linkside.app.ui.ideas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.IdeaThread
import com.linkside.app.ui.components.LinksideTopAppBar
import com.linkside.app.ui.components.PrimaryButton
import com.linkside.app.ui.theme.LinksideColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeaThreadsScreen(
    threads: List<IdeaThread>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onThreadClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = LinksideColors.Primary,
        topBar = { LinksideTopAppBar(title = "Idea Threads", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = LinksideColors.Accent,
            ) {
                Icon(Icons.Default.Add, contentDescription = "New thread", tint = LinksideColors.OnGold)
            }
        },
    ) { padding ->
        if (threads.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "No idea threads yet",
                    fontWeight = FontWeight.SemiBold,
                    color = LinksideColors.TextPrimary,
                )
                Text(
                    "Brainstorm a round with your crew — no commitment yet.",
                    color = LinksideColors.TextSecondary,
                )
                PrimaryButton(title = "Start a Thread", onClick = onCreateClick)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(threads, key = { it.id }) { thread ->
                    IdeaThreadRow(thread = thread, onClick = { onThreadClick(thread.id) })
                }
            }
        }
    }
}

@Composable
private fun IdeaThreadRow(thread: IdeaThread, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    val updated = Instant.ofEpochMilli(thread.updatedAt.toLong())
        .atZone(ZoneId.systemDefault())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LinksideColors.Card)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(thread.name, fontWeight = FontWeight.SemiBold, color = LinksideColors.TextPrimary)
            Text(
                if (thread.isBrainstorming) "Brainstorming" else "Converted",
                style = MaterialTheme.typography.labelSmall,
                color = LinksideColors.AccentLabel,
            )
        }
        Text(
            "${thread.invitees.size} member${if (thread.invitees.size == 1) "" else "s"} · Updated ${formatter.format(updated)}",
            style = MaterialTheme.typography.bodySmall,
            color = LinksideColors.TextSecondary,
        )
    }
}
