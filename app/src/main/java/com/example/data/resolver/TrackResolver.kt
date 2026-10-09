package com.example.data.resolver

import com.example.data.model.AudioFormat
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import com.example.data.source.PeerSanitizer
import com.example.data.source.SourceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class TrackResolver(
    private val sourceProvider: SourceProvider,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    // Cache for pre-resolved tracks in queue
    private val preResolvedCache = ConcurrentHashMap<String, List<SoulseekPeerSource>>()

    /**
     * Resolves a track to ranked Soulseek peer sources over a progressive ~6s search budget.
     */
    fun resolve(track: Track): Flow<List<SoulseekPeerSource>> = flow {
        // Check cache first
        preResolvedCache[track.id]?.let { cached ->
            if (cached.isNotEmpty()) {
                emit(cached)
                return@flow
            }
        }

        sourceProvider.resolveCandidates(track).collect { candidates ->
            val ranked = rankCandidates(track, candidates)
            preResolvedCache[track.id] = ranked
            emit(ranked)
        }
    }

    /**
     * Pre-resolves the next track in the queue in background so playback transitions seamlessly.
     */
    fun preResolveNext(track: Track) {
        if (preResolvedCache.containsKey(track.id)) return
        scope.launch {
            try {
                sourceProvider.resolveCandidates(track).collect { candidates ->
                    val ranked = rankCandidates(track, candidates)
                    if (ranked.isNotEmpty()) {
                        preResolvedCache[track.id] = ranked
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun getPreResolved(trackId: String): SoulseekPeerSource? {
        return preResolvedCache[trackId]?.firstOrNull()
    }

    /**
     * Scores and ranks candidates according to Sonora resolver rules:
     * 1. Correct match (title/artist, duration within 3s)
     * 2. Rejection of live/remix/karaoke unless requested
     * 3. Audio format (Lossless > 320 > V0 > lower)
     * 4. Free slot, low queue, fast peer speed
     */
    fun rankCandidates(track: Track, rawCandidates: List<SoulseekPeerSource>): List<SoulseekPeerSource> {
        val filtered = rawCandidates.filter { candidate ->
            // Path safety check
            if (!PeerSanitizer.isValidAudioFile(candidate.filename)) return@filter false

            val lowerFilename = candidate.filename.lowercase()
            val lowerTrackTitle = track.title.lowercase()

            // Reject live/remix/karaoke/instrumental unless requested in the track title
            val unwantedTags = listOf("karaoke", "instrumental", "tribute", "cover version")
            for (tag in unwantedTags) {
                if (lowerFilename.contains(tag) && !lowerTrackTitle.contains(tag)) {
                    return@filter false
                }
            }
            if (lowerFilename.contains("live") && !lowerTrackTitle.contains("live")) {
                // If peer has "live at" or "(live)", reject unless track was live
                if (lowerFilename.contains("live at") || lowerFilename.contains("(live)")) {
                    return@filter false
                }
            }
            if (lowerFilename.contains("remix") && !lowerTrackTitle.contains("remix")) {
                return@filter false
            }

            true
        }

        return filtered.map { candidate ->
            val score = calculateScore(track, candidate)
            candidate.copy(score = score)
        }.sortedByDescending { it.score }
    }

    private fun calculateScore(track: Track, candidate: SoulseekPeerSource): Double {
        var score = 0.0

        // 1. Title matching
        val normTrackTitle = track.title.lowercase().replace(Regex("[^a-z0-9]"), "")
        val normFilename = candidate.filename.lowercase().replace(Regex("[^a-z0-9]"), "")
        val normArtist = track.artist.lowercase().replace(Regex("[^a-z0-9]"), "")

        if (normFilename.contains(normTrackTitle)) {
            score += 50.0
        } else {
            // Partial match
            val words = track.title.lowercase().split(" ").filter { it.length > 2 }
            val matchedWords = words.count { candidate.filename.lowercase().contains(it) }
            score += (matchedWords.toDouble() / words.size.coerceAtLeast(1)) * 35.0
        }

        if (normFilename.contains(normArtist) || candidate.folder.lowercase().contains(track.artist.lowercase())) {
            score += 25.0
        }

        // 2. Duration check within 3s tolerance
        if (candidate.durationSec > 0 && track.durationSec > 0) {
            val diff = abs(candidate.durationSec - track.durationSec)
            when {
                diff <= 3 -> score += 40.0 // Exactly matches MusicBrainz duration
                diff <= 8 -> score += 20.0
                diff <= 15 -> score += 5.0
                else -> score -= 25.0 // Duration mismatch penalty
            }
        } else {
            score += 15.0 // neutral
        }

        // 3. Audio format quality hierarchy
        score += when (candidate.format) {
            AudioFormat.FLAC -> {
                if (candidate.bitDepth >= 24) 110.0 else 95.0
            }
            AudioFormat.ALAC, AudioFormat.WAV -> 90.0
            AudioFormat.MP3_320 -> 75.0
            AudioFormat.MP3_V0 -> 65.0
            AudioFormat.AAC_256 -> 55.0
            AudioFormat.MP3_256 -> 50.0
            AudioFormat.MP3_192 -> 35.0
            else -> 20.0
        }

        // 4. Peer availability & queue
        if (candidate.freeUploadSlots) {
            score += 35.0
        } else {
            score -= (candidate.queueLength * 4.0).coerceAtMost(30.0)
        }

        // 5. Peer speed bonus (e.g. 2000 KB/s => +20 pts)
        val speedBonus = (candidate.uploadSpeedKbps / 100.0).coerceIn(0.0, 30.0)
        score += speedBonus

        // 6. Complete album folder preference
        if (candidate.isCompleteAlbumFolder) {
            score += 30.0
        }

        return score
    }
}
