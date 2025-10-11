package com.volumemixer.service

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AppVolumePreference::class], version = 1, exportSchema = false)
abstract class AppVolumeDatabase : RoomDatabase() {
    abstract fun appVolumeDao(): AppVolumeDao

    companion object {
        @Volatile
        private var INSTANCE: AppVolumeDatabase? = null

        fun getDatabase(context: Context): AppVolumeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppVolumeDatabase::class.java,
                    "app_volume_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
