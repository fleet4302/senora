package com.example.data.source

import com.example.data.model.AudioFormat
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SimulatedSoulseekProvider : SourceProvider {

    // Authentic community peers on the Soulseek network
    private val peerProfiles = listOf(
        PeerProfile("flac_hoarder_99", 3450, true, 0, "DE", AudioFormat.FLAC, 24, 96000),
        PeerProfile("vocaloid_queen", 4800, true, 0, "JP", AudioFormat.FLAC, 24, 88200),
        PeerProfile("lossless_vault_eu", 2100, true, 0, "NL", AudioFormat.FLAC, 16, 44100),
        PeerProfile("underground_tapes", 1450, false, 2, "UK", AudioFormat.MP3_320, 16, 44100),
        PeerProfile("analog_archivist", 5200, true, 0, "US", AudioFormat.FLAC, 24, 192000),
        PeerProfile("retro_discography", 850, false, 4, "SE", AudioFormat.MP3_V0, 16, 44100),
        PeerProfile("audiophile_japan", 3800, true, 0, "JP", AudioFormat.FLAC, 16, 44100),
        PeerProfile("tape_trader_84", 920, true, 0, "CA", AudioFormat.MP3_256, 16, 44100)
    )

    // Audio stream URLs for real audio playback in Android
    private val playableAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3"
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
        val candidates = mutableListOf<SoulseekPeerSource>()
        val baseFilename = "%02d - %s - %s".format(track.trackNumber, track.artist, track.title)
        val sanitizedBase = PeerSanitizer.sanitizeFilename(baseFilename)

        // Wave 1: Immediate fast peers responding in ~1 second
        delay(700)
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
                streamUrl = playableAudioUrls[0],
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
                queueLength = 2,
                uploadSpeedKbps = peerProfiles[3].speedKbps,
                country = peerProfiles[3].country,
                streamUrl = playableAudioUrls[1],
                isCompleteAlbumFolder = false
            )
        )
        emit(candidates.toList())

        // Wave 2: Results trickling in over ~2.5 seconds
        delay(1500)
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
                streamUrl = playableAudioUrls[2],
                isCompleteAlbumFolder = true
            )
        )
        candidates.add(
            SoulseekPeerSource(
                peerUsername = peerProfiles[5].username,
                filename = "$sanitizedBase.mp3",
                folder = "Incoming/${track.artist} - ${track.title}",
                sizeBytes = 7_800_000L,
                bitrate = 245,
                format = AudioFormat.MP3_V0,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = track.durationSec - 2,
                freeUploadSlots = false,
                queueLength = 3,
                uploadSpeedKbps = peerProfiles[5].speedKbps,
                country = peerProfiles[5].country,
                streamUrl = playableAudioUrls[3],
                isCompleteAlbumFolder = false
            )
        )
        emit(candidates.toList())

        // Wave 3: Final sweep at ~4.5 seconds budget
        delay(1800)
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
                streamUrl = playableAudioUrls[4],
                isCompleteAlbumFolder = true
            )
        )
        candidates.add(
            SoulseekPeerSource(
                peerUsername = peerProfiles[6].username,
                filename = "$sanitizedBase.flac",
                folder = "Rips/${track.artist}/${track.album} [TOC AccurateRip]",
                sizeBytes = 39_100_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = track.durationSec,
                freeUploadSlots = true,
                queueLength = 0,
                uploadSpeedKbps = peerProfiles[6].speedKbps,
                country = peerProfiles[6].country,
                streamUrl = playableAudioUrls[5],
                isCompleteAlbumFolder = true
            )
        )
        emit(candidates.toList())
    }

    override fun resolveAlbumCandidates(albumTitle: String, artist: String, trackCount: Int): Flow<List<SoulseekPeerSource>> = flow {
        delay(900)
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
                uploadSpeedKbps = 3450,
                country = "DE",
                streamUrl = playableAudioUrls[0],
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
                streamUrl = playableAudioUrls[1],
                isCompleteAlbumFolder = true
            ),
            SoulseekPeerSource(
                peerUsername = "underground_tapes",
                filename = "$artist - $albumTitle [320kbps MP3]",
                folder = "Shares/$artist/$albumTitle",
                sizeBytes = 145_000_000L,
                bitrate = 320,
                format = AudioFormat.MP3_320,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = trackCount * 220,
                freeUploadSlots = false,
                queueLength = 1,
                uploadSpeedKbps = 1450,
                country = "UK",
                streamUrl = playableAudioUrls[2],
                isCompleteAlbumFolder = true
            )
        )
        emit(albumCandidates)
    }

    override suspend fun searchRawNetwork(
        query: String,
        filterFormat: String?,
        freeSlotsOnly: Boolean
    ): List<SoulseekPeerSource> {
        delay(600)
        val q = query.trim().lowercase()
        val results = mutableListOf<SoulseekPeerSource>()

        val peerPool = listOf(
            Triple("flac_hoarder_99", AudioFormat.FLAC, 3450),
            Triple("vocaloid_queen", AudioFormat.FLAC, 4800),
            Triple("lossless_vault_eu", AudioFormat.FLAC, 2100),
            Triple("underground_tapes", AudioFormat.MP3_320, 1450),
            Triple("analog_archivist", AudioFormat.FLAC, 5200),
            Triple("retro_discography", AudioFormat.MP3_V0, 850),
            Triple("techno_bunker_berlin", AudioFormat.FLAC, 4100),
            Triple("indie_cassette_club", AudioFormat.MP3_320, 1200)
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
                    filename = PeerSanitizer.sanitizeFilename("$query - Track ${idx + 1}.$ext"),
                    folder = "Shared/$query Collection/Disc ${idx % 2 + 1}",
                    sizeBytes = if (format == AudioFormat.FLAC) 42_000_000L else 9_500_000L,
                    bitrate = bitrate,
                    format = format,
                    sampleRate = sampleRate,
                    bitDepth = bitDepth,
                    durationSec = 195 + idx * 15,
                    freeUploadSlots = idx % 3 != 0,
                    queueLength = if (idx % 3 == 0) (idx % 4) + 1 else 0,
                    uploadSpeedKbps = peer.third,
                    country = if (idx % 2 == 0) "DE" else "US",
                    streamUrl = playableAudioUrls[idx % playableAudioUrls.size]
                )
            )
        }

        return results.filter { source ->
            if (freeSlotsOnly && !source.freeUploadSlots) return@filter false
            if (filterFormat != null && filterFormat != "All") {
                if (!source.format.displayName.contains(filterFormat, ignoreCase = true)) return@filter false
            }
            true
        }
    }

    override suspend fun browsePeerShares(peerUsername: String): List<SoulseekPeerSource> {
        delay(500)
        val cleanUser = PeerSanitizer.sanitizeUsername(peerUsername)
        return listOf(
            SoulseekPeerSource(
                peerUsername = cleanUser,
                filename = "01 - Radiohead - Airbag.flac",
                folder = "Music/Radiohead/OK Computer [FLAC]",
                sizeBytes = 46_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 96000,
                bitDepth = 24,
                durationSec = 284,
                freeUploadSlots = true,
                uploadSpeedKbps = 3200,
                streamUrl = playableAudioUrls[0]
            ),
            SoulseekPeerSource(
                peerUsername = cleanUser,
                filename = "02 - Radiohead - Paranoid Android.flac",
                folder = "Music/Radiohead/OK Computer [FLAC]",
                sizeBytes = 62_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 96000,
                bitDepth = 24,
                durationSec = 383,
                freeUploadSlots = true,
                uploadSpeedKbps = 3200,
                streamUrl = playableAudioUrls[1]
            ),
            SoulseekPeerSource(
                peerUsername = cleanUser,
                filename = "03 - Daft Punk - Giorgio by Moroder.flac",
                folder = "Music/Daft Punk/Random Access Memories",
                sizeBytes = 78_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 88200,
                bitDepth = 24,
                durationSec = 544,
                freeUploadSlots = true,
                uploadSpeedKbps = 3200,
                streamUrl = playableAudioUrls[2]
            ),
            SoulseekPeerSource(
                peerUsername = cleanUser,
                filename = "04 - Tame Impala - Let It Happen.flac",
                folder = "Music/Tame Impala/Currents",
                sizeBytes = 65_000_000L,
                bitrate = 1411,
                format = AudioFormat.FLAC,
                sampleRate = 44100,
                bitDepth = 16,
                durationSec = 467,
                freeUploadSlots = true,
                uploadSpeedKbps = 3200,
                streamUrl = playableAudioUrls[3]
            )
        )
    }

    override suspend fun initiateStream(source: SoulseekPeerSource): String {
        return source.streamUrl.ifEmpty { playableAudioUrls[0] }
    }
}
