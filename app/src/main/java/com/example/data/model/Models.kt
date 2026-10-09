package com.example.data.model

enum class AudioFormat(val extension: String, val displayName: String, val isLossless: Boolean) {
    FLAC("flac", "FLAC", true),
    ALAC("m4a", "ALAC", true),
    WAV("wav", "WAV", true),
    MP3_320("mp3", "320 kbps", false),
    MP3_V0("mp3", "V0 (VBR)", false),
    MP3_256("mp3", "256 kbps", false),
    MP3_192("mp3", "192 kbps", false),
    AAC_256("m4a", "AAC 256", false),
    OGG_VORBIS("ogg", "OGG", false),
    UNKNOWN("mp3", "Audio", false);

    companion object {
        fun fromExtensionAndBitrate(ext: String, bitrate: Int): AudioFormat {
            val cleanExt = ext.lowercase().removePrefix(".")
            return when (cleanExt) {
                "flac" -> FLAC
                "wav" -> WAV
                "m4a" -> if (bitrate > 500) ALAC else AAC_256
                "ogg" -> OGG_VORBIS
                "mp3" -> when {
                    bitrate >= 310 -> MP3_320
                    bitrate in 220..309 -> MP3_V0
                    bitrate in 180..219 -> MP3_192
                    else -> MP3_192
                }
                else -> UNKNOWN
            }
        }
    }
}

data class SoulseekPeerSource(
    val peerUsername: String,
    val filename: String,
    val folder: String,
    val sizeBytes: Long,
    val bitrate: Int,
    val format: AudioFormat,
    val sampleRate: Int = 44100,
    val bitDepth: Int = 16,
    val durationSec: Int = 0,
    val freeUploadSlots: Boolean = true,
    val queueLength: Int = 0,
    val uploadSpeedKbps: Int = 850,
    val country: String = "US",
    val score: Double = 0.0,
    val streamUrl: String = "",
    val isCompleteAlbumFolder: Boolean = false
) {
    val qualityLabel: String
        get() = when {
            format == AudioFormat.FLAC && bitDepth >= 24 -> "Hi-Res Lossless ${bitDepth}b/${sampleRate / 1000}kHz"
            format.isLossless -> "Lossless ${format.displayName}"
            format == AudioFormat.MP3_320 -> "MP3 320 kbps"
            format == AudioFormat.MP3_V0 -> "MP3 V0 VBR"
            else -> "${format.displayName} · ${bitrate}k"
        }

    val speedDisplay: String
        get() = if (uploadSpeedKbps >= 1000) {
            String.format("%.1f MB/s", uploadSpeedKbps / 1024.0)
        } else {
            "$uploadSpeedKbps KB/s"
        }
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: String = "",
    val durationSec: Int = 210,
    val trackNumber: Int = 1,
    val coverUrl: String? = null,
    val genre: String? = null,
    val year: Int? = 2024,
    val qualityBadge: String = "Lossless",
    val resolvedSource: SoulseekPeerSource? = null,
    val isDownloaded: Boolean = false,
    val isLibrary: Boolean = false
) {
    val durationFormatted: String
        get() {
            val m = durationSec / 60
            val s = durationSec % 60
            return "%d:%02d".format(m, s)
        }
}

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val coverUrl: String? = null,
    val year: Int? = 2024,
    val genre: String? = "Alternative",
    val trackCount: Int = 10,
    val tracks: List<Track> = emptyList(),
    val qualitySummary: String = "Lossless · Soulseek Verified",
    val releaseDate: String = "2024"
)

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val bio: String = "",
    val monthlyListeners: String = "1.2M",
    val topTracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList()
)

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

enum class TransferState(val label: String) {
    IDLE("Idle"),
    QUEUED("Queued at Peer"),
    RESOLVING("Resolving on Soulseek"),
    CONNECTING("Connecting"),
    BUFFERING("Buffering"),
    DOWNLOADING("Downloading"),
    COMPLETED("Saved to Library"),
    FAILED("Transfer Failed")
}

data class TransferItem(
    val id: String,
    val track: Track,
    val source: SoulseekPeerSource,
    val state: TransferState,
    val progress: Float = 0f,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val speedKbps: Int = 0,
    val queuePosition: Int = 0,
    val error: String? = null
)
