package com.example.data.catalog

import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.LyricLine
import com.example.data.model.Track

interface CatalogProvider {
    suspend fun getListenNowAlbums(): List<Album>
    suspend fun getBrowseTrending(): List<Album>
    suspend fun getCuratedArtists(): List<Artist>
    suspend fun searchCatalog(query: String): CatalogSearchResult
    suspend fun getAlbumDetails(albumId: String): Album?
    suspend fun getArtistDetails(artistId: String): Artist?
    suspend fun getSyncedLyrics(track: Track): List<LyricLine>
}

data class CatalogSearchResult(
    val query: String,
    val topResult: Any? = null,
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList()
)
