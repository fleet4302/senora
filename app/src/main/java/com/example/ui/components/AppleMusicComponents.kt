package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Album
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import com.example.data.player.PlaybackState
import com.example.ui.theme.FlacGold
import com.example.ui.theme.HiResCyan
import com.example.ui.theme.LosslessSilver
import com.example.ui.theme.Mp3Green
import com.example.ui.theme.SonoraDarkCard
import com.example.ui.theme.SonoraDarkGlass
import com.example.ui.theme.SonoraPink
import com.example.ui.theme.SonoraRed

@Composable
fun QualityBadge(
    quality: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        quality.contains("24-bit", ignoreCase = true) || quality.contains("Hi-Res", ignoreCase = true) ->
            HiResCyan.copy(alpha = 0.18f) to HiResCyan
        quality.contains("FLAC", ignoreCase = true) || quality.contains("Lossless", ignoreCase = true) ->
            FlacGold.copy(alpha = 0.16f) to FlacGold
        quality.contains("320", ignoreCase = true) ->
            Mp3Green.copy(alpha = 0.16f) to Mp3Green
        else ->
            LosslessSilver.copy(alpha = 0.12f) to LosslessSilver
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = quality.uppercase(),
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
fun AlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 156.dp
) {
    Column(
        modifier = modifier
            .width(size)
            .clickable(onClick = onClick)
            .testTag("album_card_${album.id}")
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(10.dp), clip = false)
                .clip(RoundedCornerShape(10.dp))
                .background(SonoraDarkCard)
        ) {
            AsyncImage(
                model = album.coverUrl,
                contentDescription = "${album.title} cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CardShelf(
    title: String,
    subtitle: String? = null,
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (subtitle != null) {
                    Text(
                        text = subtitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = SonoraRed,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View more",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums) { album ->
                AlbumCard(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}

@Composable
fun GlassPillButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true
) {
    val bgColor = if (isPrimary) SonoraRed else SonoraDarkCard
    val contentColor = if (isPrimary) Color.White else SonoraRed

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        modifier = modifier
            .height(48.dp)
            .shadow(if (isPrimary) 6.dp else 2.dp, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MiniPlayerCapsule(
    track: Track?,
    source: SoulseekPeerSource?,
    playbackState: PlaybackState,
    currentPosMs: Long,
    durationMs: Long,
    onExpandClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    val progress = if (durationMs > 0) (currentPosMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "mini_progress")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(SonoraDarkGlass)
            .clickable(onClick = onExpandClick)
            .testTag("mini_player_capsule")
    ) {
        // Subtle progress line at bottom of capsule
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(animatedProgress)
                .height(2.dp)
                .background(Brush.horizontalGradient(listOf(SonoraRed, SonoraPink)))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2C2C2E))
            ) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Artist / Resolver State
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (playbackState) {
                        is PlaybackState.Resolving -> {
                            Text(
                                text = "Resolving Soulseek peers...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SonoraRed,
                                maxLines = 1
                            )
                        }
                        is PlaybackState.QueuedAtPeer -> {
                            Text(
                                text = "Queued at @${playbackState.peer} (#${playbackState.position})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FlacGold,
                                maxLines = 1
                            )
                        }
                        is PlaybackState.Buffering -> {
                            Text(
                                text = "Buffering ${playbackState.percent}% · ${source?.qualityLabel ?: "FLAC"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HiResCyan,
                                maxLines = 1
                            )
                        }
                        else -> {
                            Text(
                                text = "${track.artist} · ${source?.qualityLabel ?: track.qualityBadge}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA1A1AA),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Play / Pause Button
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("mini_player_play_pause")
            ) {
                when (playbackState) {
                    is PlaybackState.Resolving -> {
                        CircularProgressIndicator(
                            color = SonoraRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    is PlaybackState.Playing -> {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Next Button
            IconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("mini_player_next")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
