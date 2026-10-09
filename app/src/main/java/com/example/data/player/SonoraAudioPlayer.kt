package com.example.data.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class PlaybackState {
    object Idle : PlaybackState()
    data class Resolving(val track: Track, val message: String = "Resolving best Soulseek source...") : PlaybackState()
    data class QueuedAtPeer(val track: Track, val peer: String, val position: Int) : PlaybackState()
    data class Buffering(val track: Track, val source: SoulseekPeerSource, val percent: Int) : PlaybackState()
    data class Playing(val track: Track, val source: SoulseekPeerSource) : PlaybackState()
    data class Paused(val track: Track, val source: SoulseekPeerSource) : PlaybackState()
    data class Failed(val track: Track, val error: String) : PlaybackState()
}

class SonoraAudioPlayer(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private var mediaPlayer: MediaPlayer? = null
    private var tickerJob: Job? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _currentSource = MutableStateFlow<SoulseekPeerSource?>(null)
    val currentSource: StateFlow<SoulseekPeerSource?> = _currentSource.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _bufferedPercent = MutableStateFlow(0)
    val bufferedPercent: StateFlow<Int> = _bufferedPercent.asStateFlow()

    var onTrackFinished: (() -> Unit)? = null

    fun updateResolvingState(track: Track, message: String = "Resolving best Soulseek source...") {
        _currentTrack.value = track
        _playbackState.value = PlaybackState.Resolving(track, message)
    }

    fun updateQueuedState(track: Track, peer: String, position: Int) {
        _currentTrack.value = track
        _playbackState.value = PlaybackState.QueuedAtPeer(track, peer, position)
    }

    fun playTrack(track: Track, source: SoulseekPeerSource) {
        _currentTrack.value = track
        _currentSource.value = source
        _playbackState.value = PlaybackState.Buffering(track, source, 15)

        releasePlayer()

        scope.launch(Dispatchers.IO) {
            try {
                // Simulate progressive buffer from Soulseek peer
                for (p in 20..100 step 25) {
                    _bufferedPercent.value = p
                    delay(120)
                }

                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )

                    val streamUrl = source.streamUrl.ifEmpty {
                        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                    }
                    setDataSource(streamUrl)

                    setOnBufferingUpdateListener { _, percent ->
                        _bufferedPercent.value = percent
                    }

                    setOnPreparedListener { preparedPlayer ->
                        _durationMs.value = if (preparedPlayer.duration > 0) {
                            preparedPlayer.duration.toLong()
                        } else {
                            (track.durationSec * 1000L)
                        }
                        preparedPlayer.start()
                        _playbackState.value = PlaybackState.Playing(track, source)
                        startTicker()
                    }

                    setOnCompletionListener {
                        _playbackState.value = PlaybackState.Paused(track, source)
                        onTrackFinished?.invoke()
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e("SonoraPlayer", "MediaPlayer error: what=$what, extra=$extra")
                        _playbackState.value = PlaybackState.Failed(track, "Playback error ($what)")
                        true
                    }

                    prepareAsync()
                }

                mediaPlayer = mp
            } catch (e: Exception) {
                Log.e("SonoraPlayer", "Failed to start player: ${e.message}")
                _playbackState.value = PlaybackState.Failed(track, e.message ?: "Playback failed")
            }
        }
    }

    fun togglePlayPause() {
        val current = _playbackState.value
        val track = _currentTrack.value ?: return
        val source = _currentSource.value ?: return

        when (current) {
            is PlaybackState.Playing -> {
                mediaPlayer?.pause()
                _playbackState.value = PlaybackState.Paused(track, source)
                stopTicker()
            }
            is PlaybackState.Paused -> {
                mediaPlayer?.start()
                _playbackState.value = PlaybackState.Playing(track, source)
                startTicker()
            }
            else -> {}
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { mp ->
            try {
                val clamped = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1000L))
                mp.seekTo(clamped.toInt())
                _currentPositionMs.value = clamped
            } catch (_: Exception) {}
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPositionMs.value = mp.currentPosition.toLong()
                        if (mp.duration > 0) {
                            _durationMs.value = mp.duration.toLong()
                        }
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun release() {
        releasePlayer()
    }

    private fun releasePlayer() {
        stopTicker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _currentPositionMs.value = 0L
    }
}
