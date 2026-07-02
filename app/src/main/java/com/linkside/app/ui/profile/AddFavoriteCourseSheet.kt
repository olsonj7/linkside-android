package com.linkside.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linkside.app.data.model.GolfCourse
import com.linkside.app.ui.theme.LinksideColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFavoriteCourseSheet(
    courses: List<GolfCourse>,
    isSearching: Boolean,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    onSelect: (GolfCourse) -> Unit,
) {
    var query by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LinksideColors.Primary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add Favorite Course", fontWeight = FontWeight.Bold, color = LinksideColors.TextPrimary)
            EditProfileField(
                value = query,
                onValueChange = {
                    query = it
                    onSearch(it)
                },
                placeholder = "Search courses",
            )
            when {
                isSearching -> {
                    CircularProgressIndicator(
                        color = LinksideColors.Accent,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 24.dp),
                    )
                }
                courses.isEmpty() && query.length >= 2 -> {
                    Text("No courses found.", color = LinksideColors.TextSecondary)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(courses, key = { it.placeId }) { course ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(course) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.name, fontWeight = FontWeight.Medium, color = LinksideColors.TextPrimary)
                                    course.address?.let {
                                        Text(it, color = LinksideColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Text(
                                    text = "Add",
                                    color = LinksideColors.AccentLabel,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(LinksideColors.Muted),
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
