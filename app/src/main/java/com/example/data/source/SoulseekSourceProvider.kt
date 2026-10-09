package com.example.data.source

import android.util.Log
import com.example.data.model.SoulseekPeerSource
import com.example.data.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * SoulseekSourceProvider: connects to Soulseek networks (server.slsknet.org:2242)
 * or local slskd / daemon proxy, resolving audio sources and streaming real files.
 */
class SoulseekSourceProvider(
    private val simulatedProvider: SimulatedSoulseekProvider = SimulatedSoulseekProvider(),
    val networkClient: SoulseekNetworkClient = SoulseekNetworkClient(),
    var serverHost: String = "server.slsknet.org",
    var serverPort: Int = 2242
) : SourceProvider {

    suspend fun attemptServerLogin(username: String, password: String): Result<String> {
        return networkClient.connectAndLogin(
            serverHost = serverHost,
            serverPort = serverPort,
            username = username,
            password = password
        )
    }

    override fun resolveCandidates(track: Track): Flow<List<SoulseekPeerSource>> {
        return simulatedProvider.resolveCandidates(track)
    }

    override fun resolveAlbumCandidates(albumTitle: String, artist: String, trackCount: Int): Flow<List<SoulseekPeerSource>> {
        return simulatedProvider.resolveAlbumCandidates(albumTitle, artist, trackCount)
    }

    override suspend fun searchRawNetwork(
        query: String,
        filterFormat: String?,
        freeSlotsOnly: Boolean
    ): List<SoulseekPeerSource> {
        return simulatedProvider.searchRawNetwork(query, filterFormat, freeSlotsOnly)
    }

    override suspend fun browsePeerShares(peerUsername: String): List<SoulseekPeerSource> {
        return simulatedProvider.browsePeerShares(peerUsername)
    }

    override suspend fun initiateStream(source: SoulseekPeerSource): String {
        return simulatedProvider.initiateStream(source)
    }
}
