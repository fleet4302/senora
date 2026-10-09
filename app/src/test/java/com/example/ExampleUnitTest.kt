package com.example

import com.example.data.model.AudioFormat
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import com.example.data.resolver.TrackResolver
import com.example.data.source.PeerSanitizer
import com.example.data.source.SimulatedSoulseekProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun peerSanitizer_blocksPathTraversal() {
        val dangerousPath = "../../etc/passwd"
        val sanitized = PeerSanitizer.sanitizePath(dangerousPath)
        assertFalse(sanitized.contains(".."))

        assertTrue(PeerSanitizer.isValidAudioFile("track.flac"))
        assertTrue(PeerSanitizer.isValidAudioFile("song.mp3"))
        assertFalse(PeerSanitizer.isValidAudioFile("malware.exe"))
    }

    @Test
    fun trackResolver_scoresLosslessHigherThanLossy() {
        val track = Track(
            id = "test-track",
            title = "Airbag",
            artist = "Radiohead",
            album = "OK Computer",
            durationSec = 284
        )

        val flacSource = SoulseekPeerSource(
            peerUsername = "flac_god",
            filename = "01 - Radiohead - Airbag.flac",
            folder = "Music/Radiohead",
            sizeBytes = 45000000L,
            bitrate = 1411,
            format = AudioFormat.FLAC,
            bitDepth = 24,
            durationSec = 284,
            freeUploadSlots = true,
            queueLength = 0,
            uploadSpeedKbps = 3500
        )

        val mp3Source = SoulseekPeerSource(
            peerUsername = "lossy_user",
            filename = "01 - Radiohead - Airbag.mp3",
            folder = "Music/Radiohead",
            sizeBytes = 10000000L,
            bitrate = 320,
            format = AudioFormat.MP3_320,
            bitDepth = 16,
            durationSec = 284,
            freeUploadSlots = false,
            queueLength = 3,
            uploadSpeedKbps = 1000
        )

        val resolver = TrackResolver(SimulatedSoulseekProvider())
        val ranked = resolver.rankCandidates(track, listOf(mp3Source, flacSource))

        assertEquals("FLAC source should be ranked first", "flac_god", ranked.first().peerUsername)
    }
}
