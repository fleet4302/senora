package com.example.data.library

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LibraryTrackEntity::class, TransferEntity::class, SharedFolderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SonoraDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        @Volatile
        private var INSTANCE: SonoraDatabase? = null

        fun getInstance(context: Context): SonoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SonoraDatabase::class.java,
                    "sonora_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
