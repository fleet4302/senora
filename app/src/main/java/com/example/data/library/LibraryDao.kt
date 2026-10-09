package com.example.data.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library_tracks ORDER BY addedTimestamp DESC")
    fun getAllTracks(): Flow<List<LibraryTrackEntity>>

    @Query("SELECT * FROM library_tracks WHERE id = :trackId LIMIT 1")
    suspend fun getTrackById(trackId: String): LibraryTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: LibraryTrackEntity)

    @Query("DELETE FROM library_tracks WHERE id = :trackId")
    suspend fun deleteTrack(trackId: String)

    @Query("SELECT * FROM transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity)

    @Query("DELETE FROM transfers WHERE id = :transferId")
    suspend fun deleteTransfer(transferId: String)

    @Query("DELETE FROM transfers WHERE state = 'COMPLETED'")
    suspend fun clearCompletedTransfers()

    @Query("SELECT * FROM shared_folders")
    fun getSharedFolders(): Flow<List<SharedFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedFolder(folder: SharedFolderEntity)
}
