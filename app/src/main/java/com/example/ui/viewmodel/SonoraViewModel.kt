package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogProvider
import com.example.data.catalog.CatalogSearchResult
import com.example.data.catalog.MusicBrainzCatalogProvider
import com.example.data.library.LibraryDao
import com.example.data.library.LibraryTrackEntity
import com.example.data.library.SonoraDatabase
import com.example.data.library.TransferEntity
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.LyricLine
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import com.example.data.player.PlaybackState
import com.example.data.player.SonoraAudioPlayer
import com.example.data.resolver.TrackResolver
import com.example.data.settings.UserSettings
import com.example.data.source.SimulatedSoulseekProvider
import com.example.data.source.SoulseekSourceProvider
import com.example.data.source.SourceProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavigationTab(val label: String) {
    LISTEN_NOW("Listen Now"),
    BROWSE("Browse"),
    LIBRARY("Library"),
    SEARCH("Search"),
    TRANSFERS("Transfers")
}

sealed class ScreenDestination {
    data class Tab(val tab: MainNavigationTab) : ScreenDestination()
    data class AlbumDetails(val album: Album) : ScreenDestination()
    data class ArtistDetails(val artist: Artist) : ScreenDestination()
    data class PeerShares(val peerUsername: String) : ScreenDestination()
    object Settings : ScreenDestination()
}

class SonoraViewModel(application: Application) : AndroidViewModel(application) {

    val settings = UserSettings(application)
    private val database = SonoraDatabase.getInstance(application)
    private val libraryDao: LibraryDao = database.libraryDao()

    private val catalogProvider: CatalogProvider = MusicBrainzCatalogProvider()
    private val sourceProvider: SourceProvider = SoulseekSourceProvider(SimulatedSoulseekProvider())
    private val trackResolver = TrackResolver(sourceProvider, viewModelScope)

    val player = SonoraAudioPlayer(application, viewModelScope)

    // Navigation & UI state
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Tab(MainNavigationTab.LISTEN_NOW))
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<ScreenDestination>()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _isSourcesSheetOpen = MutableStateFlow(false)
    val isSourcesSheetOpen: StateFlow<Boolean> = _isSourcesSheetOpen.asStateFlow()

    private val _isLyricsViewOpen = MutableStateFlow(false)
    val isLyricsViewOpen: StateFlow<Boolean> = _isLyricsViewOpen.asStateFlow()

    // Catalog Content
    private val _listenNowAlbums = MutableStateFlow<List<Album>>(emptyList())
    val listenNowAlbums: StateFlow<List<Album>> = _listenNowAlbums.asStateFlow()

    private val _browseTrending = MutableStateFlow<List<Album>>(emptyList())
    val browseTrending: StateFlow<List<Album>> = _browseTrending.asStateFlow()

    private val _curatedArtists = MutableStateFlow<List<Artist>>(emptyList())
    val curatedArtists: StateFlow<List<Artist>> = _curatedArtists.asStateFlow()

    // Search state
    private val _catalogSearchQuery = MutableStateFlow("")
    val catalogSearchQuery: StateFlow<String> = _catalogSearchQuery.asStateFlow()

    private val _catalogSearchResult = MutableStateFlow<CatalogSearchResult?>(null)
    val catalogSearchResult: StateFlow<CatalogSearchResult?> = _catalogSearchResult.asStateFlow()

    private val _isSearchingCatalog = MutableStateFlow(false)
    val isSearchingCatalog: StateFlow<Boolean> = _isSearchingCatalog.asStateFlow()

    // Raw Soulseek Search
    private val _rawSoulseekQuery = MutableStateFlow("")
    val rawSoulseekQuery: StateFlow<String> = _rawSoulseekQuery.asStateFlow()

    private val _rawSoulseekResults = MutableStateFlow<List<SoulseekPeerSource>>(emptyList())
    val rawSoulseekResults: StateFlow<List<SoulseekPeerSource>> = _rawSoulseekResults.asStateFlow()

    private val _isSearchingRawSoulseek = MutableStateFlow(false)
    val isSearchingRawSoulseek: StateFlow<Boolean> = _isSearchingRawSoulseek.asStateFlow()

    private val _rawFormatFilter = MutableStateFlow("All")
    val rawFormatFilter: StateFlow<String> = _rawFormatFilter.asStateFlow()

    private val _rawFreeSlotsOnly = MutableStateFlow(false)
    val rawFreeSlotsOnly: StateFlow<Boolean> = _rawFreeSlotsOnly.asStateFlow()

    // Peer Shares Browser
    private val _peerShares = MutableStateFlow<List<SoulseekPeerSource>>(emptyList())
    val peerShares: StateFlow<List<SoulseekPeerSource>> = _peerShares.asStateFlow()

    // Playback & Queue
    val playbackState = player.playbackState
    val currentTrack = player.currentTrack
    val currentSource = player.currentSource
    val currentPositionMs = player.currentPositionMs
    val durationMs = player.durationMs
    val bufferedPercent = player.bufferedPercent

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    // Resolver & Sources Sheet
    private val _currentTrackSources = MutableStateFlow<List<SoulseekPeerSource>>(emptyList())
    val currentTrackSources: StateFlow<List<SoulseekPeerSource>> = _currentTrackSources.asStateFlow()

    private val _isResolvingSources = MutableStateFlow(false)
    val isResolvingSources: StateFlow<Boolean> = _isResolvingSources.asStateFlow()

    // Synced Lyrics
    private val _syncedLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val syncedLyrics: StateFlow<List<LyricLine>> = _syncedLyrics.asStateFlow()

    // Library from Room
    val libraryTracks: StateFlow<List<LibraryTrackEntity>> = libraryDao.getAllTracks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeTransfers: StateFlow<List<TransferEntity>> = libraryDao.getAllTransfers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var searchJob: Job? = null
    private var resolveJob: Job? = null

    init {
        loadCatalogData()
        player.onTrackFinished = {
            playNextTrack()
        }
    }

    private fun loadCatalogData() {
        viewModelScope.launch {
            _listenNowAlbums.value = catalogProvider.getListenNowAlbums()
            _browseTrending.value = catalogProvider.getBrowseTrending()
            _curatedArtists.value = catalogProvider.getCuratedArtists()
        }
    }

    // Navigation methods
    fun navigateTo(destination: ScreenDestination) {
        if (_currentScreen.value != destination) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = destination
        }
    }

    fun navigateBack(): Boolean {
        return if (_screenHistory.isNotEmpty()) {
            _currentScreen.value = _screenHistory.removeAt(_screenHistory.lastIndex)
            true
        } else {
            false
        }
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun setSourcesSheetOpen(open: Boolean) {
        _isSourcesSheetOpen.value = open
    }

    fun setLyricsViewOpen(open: Boolean) {
        _isLyricsViewOpen.value = open
    }

    // Playback control
    fun playTrack(track: Track, newQueue: List<Track> = emptyList()) {
        if (newQueue.isNotEmpty()) {
            _queue.value = newQueue
        } else if (!_queue.value.any { it.id == track.id }) {
            _queue.value = listOf(track) + _queue.value
        }

        player.updateResolvingState(track, "Resolving Soulseek peers (FLAC/320)...")
        _isResolvingSources.value = true

        resolveJob?.cancel()
        resolveJob = viewModelScope.launch {
            // Check if already pre-resolved
            val preResolved = trackResolver.getPreResolved(track.id)
            if (preResolved != null) {
                _currentTrackSources.value = listOf(preResolved)
                _isResolvingSources.value = false
                player.playTrack(track, preResolved)
                fetchLyrics(track)
                preResolveNextInQueue(track)
                return@launch
            }

            var chosenCandidate: SoulseekPeerSource? = null
            trackResolver.resolve(track).collect { candidates ->
                _currentTrackSources.value = candidates
                if (chosenCandidate == null && candidates.isNotEmpty()) {
                    val best = candidates.first()
                    chosenCandidate = best
                    _isResolvingSources.value = false

                    if (!best.freeUploadSlots && best.queueLength > 0) {
                        player.updateQueuedState(track, best.peerUsername, best.queueLength)
                        delay(600) // Brief queue wait simulation
                    }

                    player.playTrack(track, best)
                    fetchLyrics(track)
                    preResolveNextInQueue(track)

                    // Record transfer in Room
                    recordTransfer(track, best)
                }
            }
            _isResolvingSources.value = false
        }
    }

    fun selectSourceOverride(source: SoulseekPeerSource) {
        val track = currentTrack.value ?: return
        _isSourcesSheetOpen.value = false
        player.playTrack(track, source)
    }

    fun togglePlayPause() {
        player.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun playNextTrack() {
        val q = _queue.value
        val cur = currentTrack.value
        if (q.isEmpty() || cur == null) return

        val currentIndex = q.indexOfFirst { it.id == cur.id }
        if (currentIndex in 0 until q.lastIndex) {
            val nextTrack = q[currentIndex + 1]
            playTrack(nextTrack, q)
        } else if (_isRepeat.value && q.isNotEmpty()) {
            playTrack(q.first(), q)
        }
    }

    fun playPreviousTrack() {
        if (currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }
        val q = _queue.value
        val cur = currentTrack.value
        if (q.isEmpty() || cur == null) return

        val currentIndex = q.indexOfFirst { it.id == cur.id }
        if (currentIndex > 0) {
            playTrack(q[currentIndex - 1], q)
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
        if (_isShuffle.value) {
            val cur = currentTrack.value
            val rest = _queue.value.filter { it.id != cur?.id }.shuffled()
            _queue.value = if (cur != null) listOf(cur) + rest else rest
        }
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    private fun preResolveNextInQueue(current: Track) {
        val q = _queue.value
        val curIdx = q.indexOfFirst { it.id == current.id }
        if (curIdx in 0 until q.lastIndex) {
            val nextTrack = q[curIdx + 1]
            trackResolver.preResolveNext(nextTrack)
        }
    }

    private fun fetchLyrics(track: Track) {
        viewModelScope.launch {
            _syncedLyrics.value = catalogProvider.getSyncedLyrics(track)
        }
    }

    // Catalog search
    fun onCatalogQueryChange(query: String) {
        _catalogSearchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _catalogSearchResult.value = null
            _isSearchingCatalog.value = false
            return
        }
        searchJob = viewModelScope.launch {
            _isSearchingCatalog.value = true
            delay(300)
            _catalogSearchResult.value = catalogProvider.searchCatalog(query)
            _isSearchingCatalog.value = false
        }
    }

    // Raw Soulseek Search
    fun searchRawSoulseek(query: String) {
        _rawSoulseekQuery.value = query
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearchingRawSoulseek.value = true
            val results = sourceProvider.searchRawNetwork(
                query = query,
                filterFormat = _rawFormatFilter.value,
                freeSlotsOnly = _rawFreeSlotsOnly.value
            )
            _rawSoulseekResults.value = results
            _isSearchingRawSoulseek.value = false
        }
    }

    fun setRawFormatFilter(format: String) {
        _rawFormatFilter.value = format
        if (_rawSoulseekQuery.value.isNotBlank()) {
            searchRawSoulseek(_rawSoulseekQuery.value)
        }
    }

    fun toggleRawFreeSlotsOnly() {
        _rawFreeSlotsOnly.value = !_rawFreeSlotsOnly.value
        if (_rawSoulseekQuery.value.isNotBlank()) {
            searchRawSoulseek(_rawSoulseekQuery.value)
        }
    }

    fun browseUserShares(peerUsername: String) {
        viewModelScope.launch {
            _peerShares.value = sourceProvider.browsePeerShares(peerUsername)
            navigateTo(ScreenDestination.PeerShares(peerUsername))
        }
    }

    // Library management
    fun addToLibrary(track: Track) {
        viewModelScope.launch {
            val entity = LibraryTrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                albumId = track.albumId,
                durationSec = track.durationSec,
                trackNumber = track.trackNumber,
                coverUrl = track.coverUrl,
                genre = track.genre,
                year = track.year,
                qualityBadge = track.qualityBadge,
                localFilePath = "/storage/emulated/0/Music/Sonora/${track.artist}/${track.album}/${track.trackNumber} ${track.title}.flac",
                peerUsername = currentSource.value?.peerUsername
            )
            libraryDao.insertTrack(entity)
        }
    }

    fun removeFromLibrary(trackId: String) {
        viewModelScope.launch {
            libraryDao.deleteTrack(trackId)
        }
    }

    private fun recordTransfer(track: Track, source: SoulseekPeerSource) {
        viewModelScope.launch {
            val transfer = TransferEntity(
                id = "trans-${System.currentTimeMillis()}",
                trackId = track.id,
                trackTitle = track.title,
                artist = track.artist,
                album = track.album,
                peerUsername = source.peerUsername,
                filename = source.filename,
                format = source.format.displayName,
                sizeBytes = source.sizeBytes,
                state = "COMPLETED",
                progress = 1.0f,
                speedKbps = source.uploadSpeedKbps,
                queuePosition = source.queueLength
            )
            libraryDao.insertTransfer(transfer)
        }
    }

    fun clearTransfers() {
        viewModelScope.launch {
            libraryDao.clearCompletedTransfers()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
