package com.volumemixer.service

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_volume_preferences")
data class AppVolumePreference(
    @PrimaryKey val packageName: String,
    val volumeLevel: Int,
    val isMuted: Boolean = false
)
