package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Album
import com.example.data.model.Track
import com.example.ui.components.GlassPillButton
import com.example.ui.components.QualityBadge
import com.example.ui.theme.SonoraDarkCard
import com.example.ui.theme.SonoraPink
import com.example.ui.theme.SonoraRed

@Composable
fun AlbumScreen(
    album: Album,
    currentTrackId: String?,
    onTrackClick: (Track, List<Track>) -> Unit,
    onBackClick: () -> Unit,
    onOpenSourcesForTrack: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
            .testTag("album_screen_${album.id}")
    ) {
        // Top Navigation
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("album_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        }

        // Album Header (Large Artwork, Title, Artist in accent color)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .shadow(20.dp, RoundedCornerShape(16.dp), clip = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SonoraDarkCard)
                ) {
                    AsyncImage(
                        model = album.coverUrl,
                        contentDescription = "${album.title} Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = album.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = SonoraPink,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${album.genre} · ${album.year} · ${album.qualitySummary}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA1A1AA)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Play & Shuffle Pill Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlassPillButton(
                        text = "Play",
                        icon = Icons.Default.PlayArrow,
                        onClick = {
                            if (album.tracks.isNotEmpty()) {
                                onTrackClick(album.tracks.first(), album.tracks)
                            }
                        },
                        isPrimary = true,
                        modifier = Modifier.weight(1f)
                    )

                    GlassPillButton(
                        text = "Shuffle",
                        icon = Icons.Default.Shuffle,
                        onClick = {
                            if (album.tracks.isNotEmpty()) {
                                onTrackClick(album.tracks.shuffled().first(), album.tracks)
                            }
                        },
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Numbered Tracks List
        if (album.tracks.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Resolving album tracklist from catalog...",
                        color = Color(0xFFA1A1AA),
                        fontSize = 13.sp
                    )
                }
            }
        }

        itemsIndexed(album.tracks) { index, track ->
            val isPlayingThis = track.id == currentTrackId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackClick(track, album.tracks) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("track_row_${track.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track Number
                Text(
                    text = "${index + 1}",
                    color = if (isPlayingThis) SonoraRed else Color(0xFF71717A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp)
                )

                // Track Title & Quality
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = if (isPlayingThis) SonoraRed else Color.White,
                        fontSize = 15.sp,
                        fontWeight = if (isPlayingThis) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QualityBadge(quality = track.qualityBadge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = track.durationFormatted,
                            color = Color(0xFF71717A),
                            fontSize = 12.sp
                        )
                    }
                }

                // Sources sheet button for this track
                IconButton(
                    onClick = { onOpenSourcesForTrack(track) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stream,
                        contentDescription = "Sources",
                        tint = if (isPlayingThis) SonoraRed else Color(0xFF71717A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
