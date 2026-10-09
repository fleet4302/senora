package com.example.data.library

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "library_tracks")
data class LibraryTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: String,
    val durationSec: Int,
    val trackNumber: Int,
    val coverUrl: String?,
    val genre: String?,
    val year: Int?,
    val qualityBadge: String,
    val localFilePath: String? = null,
    val peerUsername: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val trackTitle: String,
    val artist: String,
    val album: String,
    val peerUsername: String,
    val filename: String,
    val format: String,
    val sizeBytes: Long,
    val state: String,
    val progress: Float,
    val speedKbps: Int,
    val queuePosition: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "shared_folders")
data class SharedFolderEntity(
    @PrimaryKey val path: String,
    val fileCount: Int,
    val totalSizeBytes: Long,
    val isEnabled: Boolean = true
)
