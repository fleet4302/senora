package com.example.data.source

import android.util.Log
import com.example.data.model.AudioFormat
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SimulatedSoulseekProvider(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
) : SourceProvider {

    private val peerProfiles = listOf(
        PeerProfile("flac_hoarder_99", 4200, true, 0, "DE", AudioFormat.FLAC, 24, 96000),
        PeerProfile("vocaloid_queen", 4800, true, 0, "JP", AudioFormat.FLAC, 24, 88200),
        PeerProfile("lossless_vault_eu", 3100, true, 0, "NL", AudioFormat.FLAC, 16, 44100),
        PeerProfile("underground_tapes", 1850, false, 2, "UK", AudioFormat.MP3_320, 16, 44100),
        PeerProfile("analog_archivist", 5400, true, 0, "US", AudioFormat.FLAC, 24, 192000),
        PeerProfile("retro_discography", 950, false, 3, "SE", AudioFormat.MP3_V0, 16, 44100)
    )

    private data class PeerProfile(
        val username: String,
        val speedKbps: Int,
        val freeSlots: Boolean,
        val queueLength: Int,
        val country: String,
        val preferredFormat: AudioFormat,
        val bitDepth: Int,
        val sampleRate: Int
    )

    override fun resolveCandidates(track: Track): Flow<List<SoulseekPeerSource>> = flow {
        // Resolve the real audio stream for this exact song
        val realSongAudioUrl = track.streamUrl ?: fetchSongAudioUrl(track.artist, track.title)
        val candidates = mutableListOf<SoulseekPeerSource>()
        val baseFilename = "%02d - %s - %s".format(track.trackNumber, track.artist, track.title)
        val sanitizedBase = PeerSanitizer.sanitizeFilename(baseFilename)

        // Wave 1: Immediate fast peers responding in ~500ms
        delay(400)
        candidates.add(
            SoulseekPeerSource(
                peerUsername = peerProfiles[0].username,
                filename = "$sanitizedBase.flac",
                folder = "Music/${track.artist}/${track.album} [FLAC 24-96]",
                sizeBytes = 48_500_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 96000,
                bitDepth = 24,
                durationSec = track.durationSec,
                freeUploadSlots = true,
                queueLength = 0,
                uploadSpeedKbps = peerProfiles[0].speedKbps,
                country = peerProfiles[0].country,
                streamUrl = realSongAudioUrl,
                isCompleteAlbumFolder = true
            )
        )
        candidates.add(
            SoulseekPeerSource(
                peerUsername = peerProfiles[3].username,
                filename = "$sanitizedBase.mp3",
                folder = "Shares/Alternative/${track.artist}/${track.album} (320kbps)",
                sizeBytes = 11_200_000L,
                bitrate = 320,
                format = AudioFormat.MP3_320,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = track.durationSec + 1,
                freeUploadSlots = false,
                queueLength = 1,
                uploadSpeedKbps = peerProfiles[3].speedKbps,
                country = peerProfiles[3].country,
                streamUrl = realSongAudioUrl,
                isCompleteAlbumFolder = false
            )
        )
        emit(candidates.toList())

        // Wave 2: Results trickling in
        delay(800)
        candidates.add(
            SoulseekPeerSource(
                peerUsername = peerProfiles[1].username,
                filename = "$sanitizedBase.flac",
                folder = "Archive/${track.artist}/[2024] ${track.album}/CD1",
                sizeBytes = 42_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 88200,
                bitDepth = 24,
                durationSec = track.durationSec,
                freeUploadSlots = true,
                queueLength = 0,
                uploadSpeedKbps = peerProfiles[1].speedKbps,
                country = peerProfiles[1].country,
                streamUrl = realSongAudioUrl,
                isCompleteAlbumFolder = true
            )
        )
        emit(candidates.toList())

        // Wave 3: Final sweep
        delay(900)
        candidates.add(
            SoulseekPeerSource(
                peerUsername = peerProfiles[2].username,
                filename = "$sanitizedBase.flac",
                folder = "Lossless_P2P/${track.artist}/${track.album}",
                sizeBytes = 36_400_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = track.durationSec,
                freeUploadSlots = true,
                queueLength = 0,
                uploadSpeedKbps = peerProfiles[2].speedKbps,
                country = peerProfiles[2].country,
                streamUrl = realSongAudioUrl,
                isCompleteAlbumFolder = true
            )
        )
        emit(candidates.toList())
    }

    override fun resolveAlbumCandidates(albumTitle: String, artist: String, trackCount: Int): Flow<List<SoulseekPeerSource>> = flow {
        delay(600)
        val albumCandidates = listOf(
            SoulseekPeerSource(
                peerUsername = "flac_hoarder_99",
                filename = "$artist - $albumTitle [Complete 24-96 Lossless]",
                folder = "Music/$artist/$albumTitle [Complete]",
                sizeBytes = 540_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 96000,
                bitDepth = 24,
                durationSec = trackCount * 220,
                freeUploadSlots = true,
                queueLength = 0,
                uploadSpeedKbps = 4200,
                country = "DE",
                isCompleteAlbumFolder = true
            ),
            SoulseekPeerSource(
                peerUsername = "vocaloid_queen",
                filename = "$artist - $albumTitle [Full Album FLAC]",
                folder = "Archive/$artist/$albumTitle",
                sizeBytes = 480_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 88200,
                bitDepth = 24,
                durationSec = trackCount * 220,
                freeUploadSlots = true,
                queueLength = 0,
                uploadSpeedKbps = 4800,
                country = "JP",
                isCompleteAlbumFolder = true
            )
        )
        emit(albumCandidates)
    }

    override suspend fun searchRawNetwork(
        query: String,
        filterFormat: String?,
        freeSlotsOnly: Boolean
    ): List<SoulseekPeerSource> = withContext(Dispatchers.IO) {
        val q = query.trim()
        val realUrl = fetchSongAudioUrl("", q)
        val results = mutableListOf<SoulseekPeerSource>()

        val peerPool = listOf(
            Triple("flac_hoarder_99", AudioFormat.FLAC, 4200),
            Triple("vocaloid_queen", AudioFormat.FLAC, 4800),
            Triple("lossless_vault_eu", AudioFormat.FLAC, 3100),
            Triple("underground_tapes", AudioFormat.MP3_320, 1850),
            Triple("analog_archivist", AudioFormat.FLAC, 5400),
            Triple("retro_discography", AudioFormat.MP3_V0, 950)
        )

        for ((idx, peer) in peerPool.withIndex()) {
            val format = peer.second
            val ext = format.extension
            val sampleRate = if (format == AudioFormat.FLAC) 96000 else 44100
            val bitDepth = if (format == AudioFormat.FLAC) 24 else 16
            val bitrate = if (format == AudioFormat.FLAC) 1411 else 320

            results.add(
                SoulseekPeerSource(
                    peerUsername = peer.first,
                    filename = PeerSanitizer.sanitizeFilename("$q - Master Track ${idx + 1}.$ext"),
                    folder = "Shared/$q Collection/Disc ${idx % 2 + 1}",
                    sizeBytes = if (format == AudioFormat.FLAC) 42_000_000L else 9_500_000L,
                    bitrate = bitrate,
                    format = format,
                    sampleRate = sampleRate,
                    bitDepth = bitDepth,
                    durationSec = 210,
                    freeUploadSlots = idx % 3 != 0,
                    queueLength = if (idx % 3 == 0) 1 else 0,
                    uploadSpeedKbps = peer.third,
                    country = if (idx % 2 == 0) "DE" else "US",
                    streamUrl = realUrl
                )
            )
        }

        results.filter { source ->
            if (freeSlotsOnly && !source.freeUploadSlots) return@filter false
            if (filterFormat != null && filterFormat != "All") {
                if (!source.format.displayName.contains(filterFormat, ignoreCase = true)) return@filter false
            }
            true
        }
    }

    override suspend fun browsePeerShares(peerUsername: String): List<SoulseekPeerSource> = withContext(Dispatchers.IO) {
        val cleanUser = PeerSanitizer.sanitizeUsername(peerUsername)
        listOf(
            SoulseekPeerSource(
                peerUsername = cleanUser,
                filename = "01 - Daft Punk - One More Time.flac",
                folder = "Music/Daft Punk/Discovery [FLAC 16-44]",
                sizeBytes = 46_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = 320,
                freeUploadSlots = true,
                uploadSpeedKbps = 4200,
                streamUrl = "https://cdnt-preview.dzcdn.net/api/1/1/f/b/5/fb5f8b9ecf80fc57df84483bba7ca878.mp3"
            ),
            SoulseekPeerSource(
                peerUsername = cleanUser,
                filename = "02 - Radiohead - Airbag.flac",
                folder = "Music/Radiohead/OK Computer [24-96]",
                sizeBytes = 62_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 96000,
                bitDepth = 24,
                durationSec = 284,
                freeUploadSlots = true,
                uploadSpeedKbps = 4200,
                streamUrl = "https://cdnt-preview.dzcdn.net/api/1/1/a/2/b/a2bfa54c59a58bb0e0ffb471cb736ea2.mp3"
            ),
            SoulseekPeerSource(
                peerUsername = cleanUser,
                filename = "03 - Tame Impala - The Less I Know the Better.flac",
                folder = "Music/Tame Impala/Currents",
                sizeBytes = 38_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = 216,
                freeUploadSlots = true,
                uploadSpeedKbps = 4200,
                streamUrl = "https://cdnt-preview.dzcdn.net/api/1/1/2/4/6/2464e8ca61661d900696ebfe3d44ba54.mp3"
            )
        )
    }

    override suspend fun initiateStream(source: SoulseekPeerSource): String {
        return source.streamUrl
    }

    private suspend fun fetchSongAudioUrl(artist: String, title: String): String = withContext(Dispatchers.IO) {
        try {
            val query = "$artist $title".trim()
            val url = "https://api.deezer.com/search?q=${URLEncoder.encode(query, "UTF-8")}&limit=1"
            val req = Request.Builder().url(url).build()
            val res = httpClient.newCall(req).execute()
            if (res.isSuccessful) {
                val data = JSONObject(res.body?.string().orEmpty()).optJSONArray("data")
                if (data != null && data.length() > 0) {
                    val preview = data.getJSONObject(0).optString("preview")
                    if (preview.isNotEmpty()) return@withContext preview
                }
            }
        } catch (e: Exception) {
            Log.d("SoulseekStream", "Fallback audio fetch error: ${e.message}")
        }
        "https://cdnt-preview.dzcdn.net/api/1/1/f/b/5/fb5f8b9ecf80fc57df84483bba7ca878.mp3"
    }
}
