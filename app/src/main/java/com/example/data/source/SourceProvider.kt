package com.example.data.source

import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import kotlinx.coroutines.flow.Flow

interface SourceProvider {
    /**
     * Resolves a track to ranked Soulseek peer candidates.
     * Yields progressive results over ~6 second search window.
     */
    fun resolveCandidates(track: Track): Flow<List<SoulseekPeerSource>>

    /**
     * Resolves a complete album to Soulseek candidates, prioritizing peers
     * who share the complete folder for gapless/consistent quality.
     */
    fun resolveAlbumCandidates(albumTitle: String, artist: String, trackCount: Int): Flow<List<SoulseekPeerSource>>

    /**
     * Raw Soulseek network search (unconstrained by MusicBrainz catalog).
     * Search files, folders, users, and browse peer shares.
     */
    suspend fun searchRawNetwork(query: String, filterFormat: String? = null, freeSlotsOnly: Boolean = false): List<SoulseekPeerSource>

    /**
     * Browse a specific peer user's shared folders and files.
     */
    suspend fun browsePeerShares(peerUsername: String): List<SoulseekPeerSource>

    /**
     * Start downloading / progressive streaming of a resolved candidate.
     */
    suspend fun initiateStream(source: SoulseekPeerSource): String
}
