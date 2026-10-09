package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AudioFormat
import com.example.data.model.Track
import com.example.ui.components.MiniPlayerCapsule
import com.example.ui.components.SourcesSheet
import com.example.ui.screens.AlbumScreen
import com.example.ui.screens.ArtistScreen
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.FullScreenPlayerSheet
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ListenNowScreen
import com.example.ui.screens.PeerSharesScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransfersScreen
import com.example.ui.theme.SonoraDarkBackground
import com.example.ui.theme.SonoraDarkBar
import com.example.ui.theme.SonoraRed
import com.example.ui.theme.SonoraTheme
import com.example.ui.viewmodel.MainNavigationTab
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SonoraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonoraTheme {
                val viewModel: SonoraViewModel = viewModel()
                SonoraMainApp(viewModel)
            }
        }
    }
}

@Composable
fun SonoraMainApp(viewModel: SonoraViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val isSourcesSheetOpen by viewModel.isSourcesSheetOpen.collectAsState()

    val currentTrack by viewModel.currentTrack.collectAsState()
    val currentSource by viewModel.currentSource.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentPosMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val lyrics by viewModel.syncedLyrics.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val isRepeat by viewModel.isRepeat.collectAsState()

    val currentTrackSources by viewModel.currentTrackSources.collectAsState()
    val isResolvingSources by viewModel.isResolvingSources.collectAsState()

    // Back handler for screen navigation stack
    BackHandler(enabled = !isPlayerExpanded && currentScreen !is ScreenDestination.Tab) {
        viewModel.navigateBack()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(SonoraDarkBackground)) {
        val isWideScreen = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Adaptive Navigation Rail on Tablets / Wide screens
            if (isWideScreen) {
                NavigationRail(
                    containerColor = SonoraDarkBar,
                    contentColor = Color.White,
                    header = {
                        Text(
                            text = "Sonora",
                            color = SonoraRed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                ) {
                    MainNavigationTab.values().forEach { tab ->
                        val isSelected = currentScreen is ScreenDestination.Tab &&
                                (currentScreen as ScreenDestination.Tab).tab == tab
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(ScreenDestination.Tab(tab)) },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        MainNavigationTab.LISTEN_NOW -> Icons.Default.PlayCircleOutline
                                        MainNavigationTab.BROWSE -> Icons.Default.Widgets
                                        MainNavigationTab.LIBRARY -> Icons.Default.LibraryMusic
                                        MainNavigationTab.SEARCH -> Icons.Default.Search
                                        MainNavigationTab.TRANSFERS -> Icons.Default.Download
                                    },
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = SonoraRed,
                                indicatorColor = SonoraRed,
                                unselectedIconColor = Color(0xFFA1A1AA),
                                unselectedTextColor = Color(0xFFA1A1AA)
                            )
                        )
                    }
                }
            }

            // Main Content Area
            Scaffold(
                bottomBar = {
                    if (!isWideScreen) {
                        Column {
                            // Mini Player Capsule
                            if (currentTrack != null && !isPlayerExpanded) {
                                MiniPlayerCapsule(
                                    track = currentTrack,
                                    source = currentSource,
                                    playbackState = playbackState,
                                    currentPosMs = currentPosMs,
                                    durationMs = durationMs,
                                    onExpandClick = { viewModel.setPlayerExpanded(true) },
                                    onPlayPauseClick = { viewModel.togglePlayPause() },
                                    onNextClick = { viewModel.playNextTrack() }
                                )
                            }

                            // Apple Music style translucent Bottom Navigation Bar
                            NavigationBar(
                                containerColor = SonoraDarkBar,
                                contentColor = Color.White,
                                tonalElevation = 0.dp,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                MainNavigationTab.values().forEach { tab ->
                                    val isSelected = currentScreen is ScreenDestination.Tab &&
                                            (currentScreen as ScreenDestination.Tab).tab == tab

                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { viewModel.navigateTo(ScreenDestination.Tab(tab)) },
                                        icon = {
                                            Icon(
                                                imageVector = when (tab) {
                                                    MainNavigationTab.LISTEN_NOW -> Icons.Default.PlayCircleOutline
                                                    MainNavigationTab.BROWSE -> Icons.Default.Widgets
                                                    MainNavigationTab.LIBRARY -> Icons.Default.LibraryMusic
                                                    MainNavigationTab.SEARCH -> Icons.Default.Search
                                                    MainNavigationTab.TRANSFERS -> Icons.Default.Download
                                                },
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.label,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = SonoraRed,
                                            indicatorColor = SonoraRed,
                                            unselectedIconColor = Color(0xFFA1A1AA),
                                            unselectedTextColor = Color(0xFFA1A1AA)
                                        ),
                                        modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                },
                containerColor = SonoraDarkBackground
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (val screen = currentScreen) {
                        is ScreenDestination.Tab -> {
                            when (screen.tab) {
                                MainNavigationTab.LISTEN_NOW -> {
                                    val albums by viewModel.listenNowAlbums.collectAsState()
                                    ListenNowScreen(
                                        albums = albums,
                                        onAlbumClick = { viewModel.navigateTo(ScreenDestination.AlbumDetails(it)) },
                                        onOpenSettings = { viewModel.navigateTo(ScreenDestination.Settings) }
                                    )
                                }
                                MainNavigationTab.BROWSE -> {
                                    val trending by viewModel.browseTrending.collectAsState()
                                    val artists by viewModel.curatedArtists.collectAsState()
                                    BrowseScreen(
                                        trendingAlbums = trending,
                                        artists = artists,
                                        onAlbumClick = { viewModel.navigateTo(ScreenDestination.AlbumDetails(it)) },
                                        onArtistClick = { viewModel.navigateTo(ScreenDestination.ArtistDetails(it)) }
                                    )
                                }
                                MainNavigationTab.LIBRARY -> {
                                    val libraryTracks by viewModel.libraryTracks.collectAsState()
                                    LibraryScreen(
                                        libraryTracks = libraryTracks,
                                        onPlayTrack = { viewModel.playTrack(it) },
                                        onRemoveTrack = { viewModel.removeFromLibrary(it) }
                                    )
                                }
                                MainNavigationTab.SEARCH -> {
                                    val catQuery by viewModel.catalogSearchQuery.collectAsState()
                                    val catResult by viewModel.catalogSearchResult.collectAsState()
                                    val isSearchingCat by viewModel.isSearchingCatalog.collectAsState()
                                    val rawQuery by viewModel.rawSoulseekQuery.collectAsState()
                                    val rawResults by viewModel.rawSoulseekResults.collectAsState()
                                    val isSearchingRaw by viewModel.isSearchingRawSoulseek.collectAsState()
                                    val rawFilter by viewModel.rawFormatFilter.collectAsState()
                                    val rawFreeSlots by viewModel.rawFreeSlotsOnly.collectAsState()

                                    SearchScreen(
                                        catalogQuery = catQuery,
                                        onCatalogQueryChange = { viewModel.onCatalogQueryChange(it) },
                                        catalogResult = catResult,
                                        isSearchingCatalog = isSearchingCat,
                                        rawSoulseekQuery = rawQuery,
                                        onSearchRawSoulseek = { viewModel.searchRawSoulseek(it) },
                                        rawResults = rawResults,
                                        isSearchingRaw = isSearchingRaw,
                                        rawFormatFilter = rawFilter,
                                        onFormatFilterChange = { viewModel.setRawFormatFilter(it) },
                                        rawFreeSlotsOnly = rawFreeSlots,
                                        onToggleFreeSlots = { viewModel.toggleRawFreeSlotsOnly() },
                                        onTrackClick = { viewModel.playTrack(it) },
                                        onAlbumClick = { viewModel.navigateTo(ScreenDestination.AlbumDetails(it)) },
                                        onPlayRawSource = { source ->
                                            val syntheticTrack = Track(
                                                id = "raw-${source.peerUsername}-${source.filename.hashCode()}",
                                                title = source.filename.substringBeforeLast("."),
                                                artist = "@${source.peerUsername}",
                                                album = source.folder,
                                                qualityBadge = source.qualityLabel
                                            )
                                            viewModel.playTrack(syntheticTrack)
                                        },
                                        onBrowseUserShares = { viewModel.browseUserShares(it) }
                                    )
                                }
                                MainNavigationTab.TRANSFERS -> {
                                    val transfers by viewModel.activeTransfers.collectAsState()
                                    TransfersScreen(
                                        transfers = transfers,
                                        onClearCompleted = { viewModel.clearTransfers() }
                                    )
                                }
                            }
                        }
                        is ScreenDestination.AlbumDetails -> {
                            AlbumScreen(
                                album = screen.album,
                                currentTrackId = currentTrack?.id,
                                onTrackClick = { track, tracklist -> viewModel.playTrack(track, tracklist) },
                                onBackClick = { viewModel.navigateBack() },
                                onOpenSourcesForTrack = { track ->
                                    viewModel.playTrack(track)
                                    viewModel.setSourcesSheetOpen(true)
                                }
                            )
                        }
                        is ScreenDestination.ArtistDetails -> {
                            ArtistScreen(
                                artist = screen.artist,
                                onBackClick = { viewModel.navigateBack() },
                                onTrackClick = { track, tracklist -> viewModel.playTrack(track, tracklist) },
                                onAlbumClick = { viewModel.navigateTo(ScreenDestination.AlbumDetails(it)) }
                            )
                        }
                        is ScreenDestination.PeerShares -> {
                            val peerFiles by viewModel.peerShares.collectAsState()
                            PeerSharesScreen(
                                peerUsername = screen.peerUsername,
                                files = peerFiles,
                                onBackClick = { viewModel.navigateBack() },
                                onPlayFile = { source ->
                                    val syntheticTrack = Track(
                                        id = "peer-file-${source.filename.hashCode()}",
                                        title = source.filename.substringBeforeLast("."),
                                        artist = "@${source.peerUsername}",
                                        album = source.folder,
                                        qualityBadge = source.qualityLabel
                                    )
                                    viewModel.playTrack(syntheticTrack)
                                }
                            )
                        }
                        is ScreenDestination.Settings -> {
                            SettingsScreen(
                                settings = viewModel.settings,
                                onBackClick = { viewModel.navigateBack() }
                            )
                        }
                    }
                }
            }
        }

        // Full Screen Player Overlay (Slides up like Apple Music)
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            FullScreenPlayerSheet(
                track = currentTrack,
                source = currentSource,
                playbackState = playbackState,
                currentPosMs = currentPosMs,
                durationMs = durationMs,
                lyrics = lyrics,
                isShuffle = isShuffle,
                isRepeat = isRepeat,
                onCollapse = { viewModel.setPlayerExpanded(false) },
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.playNextTrack() },
                onPrevious = { viewModel.playPreviousTrack() },
                onSeekTo = { viewModel.seekTo(it) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onOpenSources = { viewModel.setSourcesSheetOpen(true) },
                onAddToLibrary = { currentTrack?.let { viewModel.addToLibrary(it) } }
            )
        }

        // Sources Sheet (Stremio-style stream picker)
        if (isSourcesSheetOpen) {
            SourcesSheet(
                track = currentTrack,
                currentSource = currentSource,
                candidates = currentTrackSources,
                isResolving = isResolvingSources,
                onSelectSource = { viewModel.selectSourceOverride(it) },
                onDismiss = { viewModel.setSourcesSheetOpen(false) }
            )
        }
    }
}
