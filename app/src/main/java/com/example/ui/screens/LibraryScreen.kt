package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.library.LibraryTrackEntity
import com.example.data.model.Track
import com.example.ui.components.QualityBadge
import com.example.ui.theme.SonoraDarkCard
import com.example.ui.theme.SonoraRed

@Composable
fun LibraryScreen(
    libraryTracks: List<LibraryTrackEntity>,
    onPlayTrack: (Track) -> Unit,
    onRemoveTrack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
            .testTag("library_screen")
    ) {
        item {
            Text(
                text = "Library",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Apple Music style Library Navigation Rows
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                LibraryNavRow(title = "Playlists", icon = Icons.AutoMirrored.Filled.PlaylistPlay)
                LibraryNavRow(title = "Artists", icon = Icons.Default.Person)
                LibraryNavRow(title = "Albums", icon = Icons.Default.Album)
                LibraryNavRow(title = "Songs", icon = Icons.Default.MusicNote)
                LibraryNavRow(title = "Downloaded (LRU Cache)", icon = Icons.Default.DownloadDone)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Recently Added",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        if (libraryTracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your library is empty.\nTap 'Add to Library' on any album or track.",
                        color = Color(0xFF71717A),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(libraryTracks) { entity ->
                val track = Track(
                    id = entity.id,
                    title = entity.title,
                    artist = entity.artist,
                    album = entity.album,
                    albumId = entity.albumId,
                    durationSec = entity.durationSec,
                    trackNumber = entity.trackNumber,
                    coverUrl = entity.coverUrl,
                    genre = entity.genre,
                    year = entity.year,
                    qualityBadge = entity.qualityBadge,
                    isLibrary = true,
                    isDownloaded = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayTrack(track) }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entity.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            QualityBadge(quality = entity.qualityBadge)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${entity.artist} · ${entity.album}",
                                color = Color(0xFFA1A1AA),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { onRemoveTrack(entity.id) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove",
                            tint = Color(0xFF71717A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onPlayTrack(track) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = SonoraRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryNavRow(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SonoraRed,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}
