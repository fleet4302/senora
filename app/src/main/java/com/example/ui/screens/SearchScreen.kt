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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.CatalogSearchResult
import com.example.data.model.Album
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import com.example.ui.components.QualityBadge
import com.example.ui.theme.PeerActive
import com.example.ui.theme.PeerBusy
import com.example.ui.theme.SonoraDarkCard
import com.example.ui.theme.SonoraDarkSurface
import com.example.ui.theme.SonoraPink
import com.example.ui.theme.SonoraRed

@Composable
fun SearchScreen(
    catalogQuery: String,
    onCatalogQueryChange: (String) -> Unit,
    catalogResult: CatalogSearchResult?,
    isSearchingCatalog: Boolean,
    rawSoulseekQuery: String,
    onSearchRawSoulseek: (String) -> Unit,
    rawResults: List<SoulseekPeerSource>,
    isSearchingRaw: Boolean,
    rawFormatFilter: String,
    onFormatFilterChange: (String) -> Unit,
    rawFreeSlotsOnly: Boolean,
    onToggleFreeSlots: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onPlayRawSource: (SoulseekPeerSource) -> Unit,
    onBrowseUserShares: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScopeIndex by remember { mutableIntStateOf(0) } // 0: Catalog, 1: Raw Soulseek

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
            .testTag("search_screen")
    ) {
        // Title
        Text(
            text = "Search",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Scope Switch Tabs: Catalog vs Raw Soulseek
        TabRow(
            selectedTabIndex = selectedScopeIndex,
            containerColor = Color.Transparent,
            contentColor = SonoraRed,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedScopeIndex]),
                    color = SonoraRed
                )
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Tab(
                selected = selectedScopeIndex == 0,
                onClick = { selectedScopeIndex = 0 },
                text = {
                    Text(
                        text = "Catalog (MusicBrainz)",
                        fontWeight = if (selectedScopeIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedScopeIndex == 0) SonoraRed else Color(0xFFA1A1AA)
                    )
                },
                modifier = Modifier.testTag("tab_catalog_search")
            )
            Tab(
                selected = selectedScopeIndex == 1,
                onClick = { selectedScopeIndex = 1 },
                text = {
                    Text(
                        text = "Raw Soulseek Network",
                        fontWeight = if (selectedScopeIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedScopeIndex == 1) SonoraRed else Color(0xFFA1A1AA)
                    )
                },
                modifier = Modifier.testTag("tab_raw_soulseek_search")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedScopeIndex == 0) {
            // CATALOG SEARCH
            OutlinedTextField(
                value = catalogQuery,
                onValueChange = onCatalogQueryChange,
                placeholder = { Text("Artists, Songs, Albums...", color = Color(0xFF71717A)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFA1A1AA)
                    )
                },
                trailingIcon = {
                    if (catalogQuery.isNotEmpty()) {
                        IconButton(onClick = { onCatalogQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SonoraDarkCard,
                    unfocusedContainerColor = SonoraDarkCard,
                    focusedBorderColor = SonoraRed,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("catalog_search_input")
            )

            if (isSearchingCatalog) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SonoraRed, strokeWidth = 2.dp)
                }
            } else if (catalogResult != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Tracks section
                    if (catalogResult.tracks.isNotEmpty()) {
                        item {
                            Text(
                                text = "Songs",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(catalogResult.tracks) { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTrackClick(track) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${track.artist} · ${track.album}",
                                        color = Color(0xFFA1A1AA),
                                        fontSize = 12.sp
                                    )
                                }
                                QualityBadge(quality = track.qualityBadge)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = SonoraRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Albums section
                    if (catalogResult.albums.isNotEmpty()) {
                        item {
                            Text(
                                text = "Albums",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(catalogResult.albums) { album ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAlbumClick(album) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = album.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${album.artist} · ${album.year}",
                                        color = Color(0xFFA1A1AA),
                                        fontSize = 12.sp
                                    )
                                }
                                QualityBadge(quality = "LOSSLESS")
                            }
                        }
                    }
                }
            } else {
                // Suggestions prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Search across millions of MusicBrainz releases\nand stream directly from Soulseek peers.",
                        color = Color(0xFF71717A),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // RAW SOULSEEK NETWORK SEARCH
            OutlinedTextField(
                value = rawSoulseekQuery,
                onValueChange = onSearchRawSoulseek,
                placeholder = { Text("Search files, folders, rare rips...", color = Color(0xFF71717A)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFA1A1AA)
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SonoraDarkCard,
                    unfocusedContainerColor = SonoraDarkCard,
                    focusedBorderColor = SonoraRed,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("raw_soulseek_search_input")
            )

            // Format filter chips & Free slots toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("All", "FLAC", "320", "V0").forEach { fmt ->
                    FilterChip(
                        selected = rawFormatFilter == fmt,
                        onClick = { onFormatFilterChange(fmt) },
                        label = { Text(fmt, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SonoraRed,
                            selectedLabelColor = Color.White,
                            containerColor = SonoraDarkCard,
                            labelColor = Color(0xFFA1A1AA)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Free Slots",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = rawFreeSlotsOnly,
                        onCheckedChange = { onToggleFreeSlots() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SonoraRed
                        )
                    )
                }
            }

            if (isSearchingRaw) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SonoraRed, strokeWidth = 2.dp)
                }
            } else if (rawResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rawResults) { source ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SonoraDarkCard)
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    QualityBadge(quality = source.qualityLabel)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "@${source.peerUsername}",
                                        color = if (source.freeUploadSlots) PeerActive else PeerBusy,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = source.speedDisplay,
                                        color = Color(0xFFA1A1AA),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = source.filename,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = source.folder,
                                    color = Color(0xFF71717A),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { onBrowseUserShares(source.peerUsername) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderShared,
                                            contentDescription = "Browse Shares",
                                            tint = SonoraPink,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { onPlayRawSource(source) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Stream",
                                            tint = SonoraRed,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Type any artist, song, or folder name to query\nconnected Soulseek peers directly in real time.",
                        color = Color(0xFF71717A),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
