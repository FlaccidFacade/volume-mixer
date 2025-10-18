package com.volumemixer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.volumemixer.MainActivity
import com.volumemixer.R

class AudioSessionService : Service() {

    companion object {
        private const val TAG = "AudioSessionService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "volume_mixer_channel"
        private const val CHANNEL_NAME = "Volume Mixer"
        
        const val ACTION_START = "com.volumemixer.action.START"
        const val ACTION_STOP = "com.volumemixer.action.STOP"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "Service running")
                startForeground(NOTIFICATION_ID, createNotification())
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service stopped")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent notification for Volume Mixer service"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Stub function for tracking audio sessions.
     * This will be implemented in future iterations.
     */
    private fun trackAudioSessions() {
        // TODO: Implement audio session tracking
        Log.d(TAG, "Audio session tracking - stub")
    }

    /**
     * Stub function for stopping audio session tracking.
     * This will be implemented in future iterations.
     */
    private fun stopTrackingSessions() {
        // TODO: Implement stop tracking
        Log.d(TAG, "Stop audio session tracking - stub")
    }
}
