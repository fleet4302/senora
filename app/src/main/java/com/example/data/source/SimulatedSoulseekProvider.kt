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
        .readTimeout(6, TimeUnit.SECONDS)
        .build()
) : SourceProvider {

    private val peerProfiles = listOf(
        PeerProfile("flac_hoarder_99", 4200, true, 0, "DE", AudioFormat.FLAC, 24, 96000),
        PeerProfile("vocaloid_queen", 4800, true, 0, "JP", AudioFormat.FLAC, 24, 88200),
        PeerProfile("lossless_vault_eu", 3100, true, 0, "NL", AudioFormat.FLAC, 16, 44100),
        PeerProfile("underground_tapes", 1850, false, 1, "UK", AudioFormat.MP3_320, 16, 44100),
        PeerProfile("analog_archivist", 5400, true, 0, "US", AudioFormat.FLAC, 24, 192000),
        PeerProfile("retro_discography", 950, false, 2, "SE", AudioFormat.MP3_V0, 16, 44100)
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
        // Resolve the authentic audio stream for this exact song
        val realSongAudioUrl = if (!track.streamUrl.isNullOrBlank()) {
            track.streamUrl
        } else {
            fetchSongAudioUrl(track.artist, track.title)
        }

        val candidates = mutableListOf<SoulseekPeerSource>()
        val baseFilename = "%02d - %s - %s".format(
            if (track.trackNumber > 0) track.trackNumber else 1,
            track.artist.replace("/", "-"),
            track.title.replace("/", "-")
        )
        val sanitizedBase = PeerSanitizer.sanitizeFilename(baseFilename)

        // Wave 1: Immediate fast peer response in ~350ms
        delay(350)
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
                durationSec = track.durationSec,
                freeUploadSlots = false,
                queueLength = 1,
                uploadSpeedKbps = peerProfiles[3].speedKbps,
                country = peerProfiles[3].country,
                streamUrl = realSongAudioUrl,
                isCompleteAlbumFolder = false
            )
        )
        emit(candidates.toList())

        // Wave 2: Results trickling in (~700ms)
        delay(350)
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

        // Wave 3: Final sweep (~1s total)
        delay(350)
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
        candidates.add(
            SoulseekPeerSource(
                peerUsername = peerProfiles[5].username,
                filename = "$sanitizedBase.mp3",
                folder = "Shares/MP3/${track.artist}/${track.album}",
                sizeBytes = 7_800_000L,
                bitrate = 245,
                format = AudioFormat.MP3_V0,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = track.durationSec,
                freeUploadSlots = false,
                queueLength = 2,
                uploadSpeedKbps = peerProfiles[5].speedKbps,
                country = peerProfiles[5].country,
                streamUrl = realSongAudioUrl,
                isCompleteAlbumFolder = false
            )
        )
        emit(candidates.toList())
    }

    override fun resolveAlbumCandidates(albumTitle: String, artist: String, trackCount: Int): Flow<List<SoulseekPeerSource>> = flow {
        delay(400)
        val albumCandidates = listOf(
            SoulseekPeerSource(
                peerUsername = "flac_hoarder_99",
                filename = "$artist - $albumTitle [Complete Album FLAC]",
                folder = "Music/$artist/$albumTitle [FLAC 24-96]",
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
        if (q.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<SoulseekPeerSource>()

        val peerPool = listOf(
            Triple("flac_hoarder_99", AudioFormat.FLAC, 4200),
            Triple("vocaloid_queen", AudioFormat.FLAC, 4800),
            Triple("lossless_vault_eu", AudioFormat.FLAC, 3100),
            Triple("underground_tapes", AudioFormat.MP3_320, 1850),
            Triple("analog_archivist", AudioFormat.FLAC, 5400),
            Triple("retro_discography", AudioFormat.MP3_V0, 950)
        )

        // Query real tracks from iTunes API for this query
        try {
            val url = "https://itunes.apple.com/search?term=${URLEncoder.encode(q, "UTF-8")}&limit=20&entity=song"
            val req = Request.Builder().url(url).build()
            val res = httpClient.newCall(req).execute()
            if (res.isSuccessful) {
                val json = JSONObject(res.body?.string().orEmpty())
                val data = json.optJSONArray("results")
                if (data != null && data.length() > 0) {
                    for (i in 0 until data.length()) {
                        val t = data.getJSONObject(i)
                        val title = t.optString("trackName", "")
                        val artist = t.optString("artistName", "Unknown Artist")
                        val album = t.optString("collectionName", "Album")
                        val preview = t.optString("previewUrl", "")
                        val durMs = t.optLong("trackTimeMillis", 210000L)
                        val trackNum = t.optInt("trackNumber", i + 1)

                        if (title.isNotBlank()) {
                            val peer = peerPool[i % peerPool.size]
                            val format = peer.second
                            val ext = format.extension
                            val filename = "%02d - %s - %s.%s".format(trackNum, artist.replace("/", "-"), title.replace("/", "-"), ext)

                            val candidate = SoulseekPeerSource(
                                peerUsername = peer.first,
                                filename = PeerSanitizer.sanitizeFilename(filename),
                                folder = "Music/$artist/$album [${format.displayName}]",
                                sizeBytes = if (format.isLossless) 44_000_000L else 9_500_000L,
                                bitrate = if (format.isLossless) 1411 else 320,
                                format = format,
                                sampleRate = if (format.isLossless) 96000 else 44100,
                                bitDepth = if (format.isLossless) 24 else 16,
                                durationSec = (durMs / 1000).toInt(),
                                freeUploadSlots = i % 3 != 0,
                                queueLength = if (i % 3 == 0) 1 else 0,
                                uploadSpeedKbps = peer.third,
                                country = if (i % 2 == 0) "DE" else "US",
                                streamUrl = preview
                            )
                            results.add(candidate)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("SoulseekSearch", "iTunes search error during raw search: ${e.message}")
        }

        // Apply filters
        var filtered = results.toList()
        if (!filterFormat.isNullOrBlank() && filterFormat != "All") {
            filtered = filtered.filter {
                when (filterFormat.uppercase()) {
                    "FLAC" -> it.format == AudioFormat.FLAC
                    "320" -> it.format == AudioFormat.MP3_320
                    "V0" -> it.format == AudioFormat.MP3_V0
                    else -> true
                }
            }
        }
        if (freeSlotsOnly) {
            filtered = filtered.filter { it.freeUploadSlots }
        }

        filtered
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
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/5d/93/d8/5d93d83f-ad1e-da4d-1d79-9937bdff24ec/mzaf_14396932211949300852.plus.aac.p.m4a"
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
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/f1/ed/05/f1ed0562-876c-a84a-c4ff-70613135b818/mzaf_18061085714629562351.plus.aac.p.m4a"
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
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/8b/55/f3/8b55f3a3-3204-8930-f156-82843546950e/mzaf_9370328603131228430.plus.aac.p.m4a"
            ),
            SoulseekPeerSource(
                peerUsername = cleanUser,
                filename = "04 - Kendrick Lamar - Alright.flac",
                folder = "Music/Kendrick Lamar/To Pimp a Butterfly",
                sizeBytes = 42_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = 219,
                freeUploadSlots = true,
                uploadSpeedKbps = 4200,
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/3b/27/4e/3b274eab-c2de-84c5-a68d-4f78f3269bac/mzaf_16117050990489545534.plus.aac.p.m4a"
            )
        )
    }

    override suspend fun initiateStream(source: SoulseekPeerSource): String {
        return source.streamUrl
    }

    suspend fun fetchSongAudioUrl(artist: String, title: String): String = withContext(Dispatchers.IO) {
        val query = "$artist $title".trim()
        if (query.isNotEmpty()) {
            try {
                val url = "https://itunes.apple.com/search?term=${URLEncoder.encode(query, "UTF-8")}&limit=1&entity=song"
                val req = Request.Builder().url(url).build()
                val res = httpClient.newCall(req).execute()
                if (res.isSuccessful) {
                    val json = JSONObject(res.body?.string().orEmpty())
                    val data = json.optJSONArray("results")
                    if (data != null && data.length() > 0) {
                        val preview = data.getJSONObject(0).optString("previewUrl")
                        if (preview.isNotEmpty()) return@withContext preview
                    }
                }
            } catch (e: Exception) {
                Log.d("SoulseekStream", "iTunes audio fetch note: ${e.message}")
            }
        }

        // Try title only if query had artist
        if (title.isNotBlank()) {
            try {
                val url = "https://itunes.apple.com/search?term=${URLEncoder.encode(title, "UTF-8")}&limit=1&entity=song"
                val req = Request.Builder().url(url).build()
                val res = httpClient.newCall(req).execute()
                if (res.isSuccessful) {
                    val json = JSONObject(res.body?.string().orEmpty())
                    val data = json.optJSONArray("results")
                    if (data != null && data.length() > 0) {
                        val preview = data.getJSONObject(0).optString("previewUrl")
                        if (preview.isNotEmpty()) return@withContext preview
                    }
                }
            } catch (_: Exception) {}
        }

        ""
    }
}
