package com.volumemixer.service

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppVolumeDao {
    @Query("SELECT * FROM app_volume_preferences")
    fun getAllPreferences(): Flow<List<AppVolumePreference>>

    @Query("SELECT * FROM app_volume_preferences WHERE packageName = :packageName")
    suspend fun getPreference(packageName: String): AppVolumePreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(preference: AppVolumePreference)

    @Query("DELETE FROM app_volume_preferences WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
