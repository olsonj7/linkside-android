package com.linkside.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.linkside.app.ui.theme.LinksideColors
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/**
 * 60×60 course thumbnail — mirrors iOS `CoursePhotoThumbnail`
 * (up to 4 attempts, 3s apart for cold server photo-ref cache).
 */
@Composable
fun CoursePhotoThumbnail(
    url: String?,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    cornerRadius: Dp = 10.dp,
) {
    if (url.isNullOrBlank()) return
    var attempt by remember(url) { mutableIntStateOf(0) }
    val context = LocalContext.current

    key(url, attempt) {
        val request = remember(url, attempt) {
            ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                // Bust caches on retry so we don't keep serving a JSON 404 body
                .memoryCacheKey("$url#$attempt")
                .diskCacheKey("$url#$attempt")
                .memoryCachePolicy(if (attempt == 0) CachePolicy.ENABLED else CachePolicy.WRITE_ONLY)
                .diskCachePolicy(if (attempt == 0) CachePolicy.ENABLED else CachePolicy.WRITE_ONLY)
                .build()
        }

        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius)),
            contentScale = ContentScale.Crop,
            loading = {
                // Spinner only on first try — retries show placeholder (matches quieter iOS failure UI)
                if (attempt == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LinksideColors.Muted),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = LinksideColors.Accent,
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(size * 0.35f),
                        )
                    }
                } else {
                    CoursePhotoPlaceholder(iconSize = size * 0.4f)
                }
            },
            error = {
                CoursePhotoPlaceholder(iconSize = size * 0.4f)
                LaunchedEffect(attempt) {
                    if (attempt < 4) {
                        delay(3_000)
                        attempt += 1
                    }
                }
            },
            success = { SubcomposeAsyncImageContent() },
        )
    }
}

/**
 * Full-width course hero — mirrors iOS `CourseHeroPhoto` (180dp, bottom gradient, retry).
 */
@Composable
fun CourseHeroPhoto(
    url: String?,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
) {
    if (url.isNullOrBlank()) return
    var attempt by remember(url) { mutableIntStateOf(0) }
    val context = LocalContext.current
    val gradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
    )

    key(url, attempt) {
        val request = remember(url, attempt) {
            ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .memoryCacheKey("hero:$url#$attempt")
                .diskCacheKey("hero:$url#$attempt")
                .memoryCachePolicy(if (attempt == 0) CachePolicy.ENABLED else CachePolicy.WRITE_ONLY)
                .diskCachePolicy(if (attempt == 0) CachePolicy.ENABLED else CachePolicy.WRITE_ONLY)
                .build()
        }

        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(14.dp))
                .drawWithContent {
                    drawContent()
                    drawRect(brush = gradient)
                },
            contentScale = ContentScale.Crop,
            loading = {
                if (attempt == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LinksideColors.Muted),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = LinksideColors.Accent, strokeWidth = 2.dp)
                    }
                } else {
                    CoursePhotoPlaceholder(iconSize = 40.dp)
                }
            },
            error = {
                CoursePhotoPlaceholder(iconSize = 40.dp)
                LaunchedEffect(attempt) {
                    if (attempt < 4) {
                        delay(3_000)
                        attempt += 1
                    }
                }
            },
            success = { SubcomposeAsyncImageContent() },
        )
    }
}

@Composable
private fun CoursePhotoPlaceholder(
    modifier: Modifier = Modifier.fillMaxSize(),
    iconSize: Dp,
) {
    Box(
        modifier = modifier.background(LinksideColors.Muted),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.GolfCourse,
            contentDescription = null,
            tint = LinksideColors.TextTertiary,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** Shared OkHttp for Coil — keeps hangs from spinning forever. */
internal fun coursePhotoOkHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()
