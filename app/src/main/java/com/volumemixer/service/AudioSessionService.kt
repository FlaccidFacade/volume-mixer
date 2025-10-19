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
        const val ACTION_SESSION_CHANGED = "com.volumemixer.action.SESSION_CHANGED"
        const val EXTRA_SESSION_DATA = "com.volumemixer.extra.SESSION_DATA"
    }

    private var mediaSessionTracker: MediaSessionTracker? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "Service starting")
                startForeground(NOTIFICATION_ID, createNotification())
                trackAudioSessions()
            }
            ACTION_STOP -> {
                Log.d(TAG, "Service stopping")
                stopTrackingSessions()
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
        stopTrackingSessions()
        Log.d(TAG, "Service destroyed")
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
     * Start tracking audio sessions using MediaSessionManager.
     */
    private fun trackAudioSessions() {
        if (mediaSessionTracker == null) {
            mediaSessionTracker = MediaSessionTracker(this).apply {
                startTracking(object : MediaSessionTracker.OnSessionChangedListener {
                    override fun onSessionsChanged(sessions: Map<String, MediaSessionTracker.MediaSessionInfo>) {
                        handleSessionsChanged(sessions)
                    }
                })
            }
            Log.d(TAG, "Media session tracking started")
        }
    }

    /**
     * Stop tracking audio sessions.
     */
    private fun stopTrackingSessions() {
        mediaSessionTracker?.let {
            it.stopTracking()
            mediaSessionTracker = null
            Log.d(TAG, "Media session tracking stopped")
        }
    }

    /**
     * Handle changes in media sessions.
     * Logs active sessions and broadcasts changes to UI.
     */
    private fun handleSessionsChanged(sessions: Map<String, MediaSessionTracker.MediaSessionInfo>) {
        Log.d(TAG, "=== Active Media Sessions ===")
        if (sessions.isEmpty()) {
            Log.d(TAG, "No active media sessions")
        } else {
            sessions.forEach { (packageName, info) ->
                Log.d(TAG, "Package: $packageName")
                Log.d(TAG, "  Session ID: ${info.sessionId}")
                Log.d(TAG, "  Playback State: ${getPlaybackStateName(info.playbackState)}")
                Log.d(TAG, "  Is Playing: ${info.isPlaying}")
            }
        }
        Log.d(TAG, "=============================")

        // Broadcast session change to UI
        broadcastSessionChange(sessions)
    }

    /**
     * Broadcast session changes to the UI.
     */
    private fun broadcastSessionChange(sessions: Map<String, MediaSessionTracker.MediaSessionInfo>) {
        val intent = Intent(ACTION_SESSION_CHANGED).apply {
            // Create a simple list of package names for the UI
            val sessionData = sessions.map { (packageName, info) ->
                "$packageName:${info.isPlaying}"
            }.toTypedArray()
            putExtra(EXTRA_SESSION_DATA, sessionData)
        }
        sendBroadcast(intent)
    }

    /**
     * Convert playback state constant to readable name.
     */
    private fun getPlaybackStateName(state: Int): String {
        return when (state) {
            android.media.session.PlaybackState.STATE_NONE -> "NONE"
            android.media.session.PlaybackState.STATE_STOPPED -> "STOPPED"
            android.media.session.PlaybackState.STATE_PAUSED -> "PAUSED"
            android.media.session.PlaybackState.STATE_PLAYING -> "PLAYING"
            android.media.session.PlaybackState.STATE_FAST_FORWARDING -> "FAST_FORWARDING"
            android.media.session.PlaybackState.STATE_REWINDING -> "REWINDING"
            android.media.session.PlaybackState.STATE_BUFFERING -> "BUFFERING"
            android.media.session.PlaybackState.STATE_ERROR -> "ERROR"
            android.media.session.PlaybackState.STATE_CONNECTING -> "CONNECTING"
            android.media.session.PlaybackState.STATE_SKIPPING_TO_PREVIOUS -> "SKIPPING_TO_PREVIOUS"
            android.media.session.PlaybackState.STATE_SKIPPING_TO_NEXT -> "SKIPPING_TO_NEXT"
            android.media.session.PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM -> "SKIPPING_TO_QUEUE_ITEM"
            else -> "UNKNOWN($state)"
        }
    }
}
