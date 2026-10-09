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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Album
import com.example.ui.components.CardShelf
import com.example.ui.components.QualityBadge
import com.example.ui.theme.PeerActive
import com.example.ui.theme.SonoraDarkCard
import com.example.ui.theme.SonoraPink
import com.example.ui.theme.SonoraRed

@Composable
fun ListenNowScreen(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val heroAlbum = albums.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
            .testTag("listen_now_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sonora",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(PeerActive)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Soulseek Network Connected",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hero Spotlight Card
        if (heroAlbum != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(200.dp)
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAlbumClick(heroAlbum) }
                    .testTag("hero_spotlight_card")
            ) {
                AsyncImage(
                    model = heroAlbum.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xE60A0A0C))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    QualityBadge(quality = "SPOTLIGHT · LOSSLESS FLAC")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = heroAlbum.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Text(
                        text = "${heroAlbum.artist} · ${heroAlbum.genre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD4D4D8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Shelves
        CardShelf(
            title = "Top Picks for You",
            subtitle = "Curated & Resolved",
            albums = albums,
            onAlbumClick = onAlbumClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        CardShelf(
            title = "Soulseek Swarm Favorites",
            subtitle = "High Speed Peers",
            albums = albums.reversed(),
            onAlbumClick = onAlbumClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        CardShelf(
            title = "Audiophile Archive",
            subtitle = "24-Bit FLAC",
            albums = albums.shuffled(),
            onAlbumClick = onAlbumClick
        )
    }
}
