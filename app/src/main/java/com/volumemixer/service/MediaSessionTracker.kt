package com.volumemixer.service

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks active media sessions using MediaSessionManager.
 * Maintains a map of package names to their session information and playback states.
 */
class MediaSessionTracker(
    private val context: Context
) : MediaSessionManager.OnActiveSessionsChangedListener {

    companion object {
        private const val TAG = "MediaSessionTracker"
    }

    private val mediaSessionManager: MediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    // Map of packageName to MediaSessionInfo
    private val activeSessions = ConcurrentHashMap<String, MediaSessionInfo>()

    // Listener for session changes
    private var onSessionChangedListener: OnSessionChangedListener? = null

    /**
     * Data class to hold session information
     */
    data class MediaSessionInfo(
        val packageName: String,
        val sessionId: String,
        val playbackState: Int,
        val isPlaying: Boolean
    )

    /**
     * Listener interface for session changes
     */
    interface OnSessionChangedListener {
        fun onSessionsChanged(sessions: Map<String, MediaSessionInfo>)
    }

    /**
     * Start tracking media sessions
     */
    fun startTracking(listener: OnSessionChangedListener? = null) {
        this.onSessionChangedListener = listener
        
        try {
            // Register listener for session changes
            val notificationListenerComponent = ComponentName(context, MediaNotificationListenerService::class.java)
            mediaSessionManager.addOnActiveSessionsChangedListener(this, notificationListenerComponent)
            
            Log.d(TAG, "Started tracking media sessions")
            
            // Immediately get current active sessions
            updateActiveSessions()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Missing notification listener permission. " +
                "Please enable notification access in Settings > Notification Access", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting media session tracking", e)
        }
    }

    /**
     * Stop tracking media sessions
     */
    fun stopTracking() {
        try {
            mediaSessionManager.removeOnActiveSessionsChangedListener(this)
            activeSessions.clear()
            onSessionChangedListener = null
            Log.d(TAG, "Stopped tracking media sessions")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media session tracking", e)
        }
    }

    /**
     * Called when active sessions change
     */
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        Log.d(TAG, "Active sessions changed, count: ${controllers?.size ?: 0}")
        updateActiveSessions(controllers)
    }

    /**
     * Update the map of active sessions
     */
    private fun updateActiveSessions(controllers: List<MediaController>? = null) {
        val currentControllers = controllers ?: try {
            val notificationListenerComponent = ComponentName(context, MediaNotificationListenerService::class.java)
            mediaSessionManager.getActiveSessions(notificationListenerComponent)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Missing notification listener permission. " +
                "Please enable notification access in Settings > Notification Access", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active sessions", e)
            emptyList()
        }

        // Clear existing sessions
        val previousPackages = activeSessions.keys.toSet()
        activeSessions.clear()

        // Add current sessions
        currentControllers.forEach { controller ->
            val packageName = controller.packageName
            val sessionToken = controller.sessionToken.toString()
            val playbackState = controller.playbackState
            
            val state = playbackState?.state ?: PlaybackState.STATE_NONE
            val isPlaying = state == PlaybackState.STATE_PLAYING

            val sessionInfo = MediaSessionInfo(
                packageName = packageName,
                sessionId = sessionToken,
                playbackState = state,
                isPlaying = isPlaying
            )

            activeSessions[packageName] = sessionInfo
            
            Log.d(TAG, "Active session: $packageName, playing: $isPlaying, state: $state")
        }

        // Log removed sessions
        val currentPackages = activeSessions.keys
        val removedPackages = previousPackages - currentPackages
        removedPackages.forEach { packageName ->
            Log.d(TAG, "Session removed: $packageName")
        }

        // Notify listener
        onSessionChangedListener?.onSessionsChanged(HashMap(activeSessions))
    }

    /**
     * Get all active sessions
     */
    fun getActiveSessions(): Map<String, MediaSessionInfo> {
        return HashMap(activeSessions)
    }

    /**
     * Get session info for a specific package
     */
    fun getSessionForPackage(packageName: String): MediaSessionInfo? {
        return activeSessions[packageName]
    }

    /**
     * Check if a package has an active session
     */
    fun hasActiveSession(packageName: String): Boolean {
        return activeSessions.containsKey(packageName)
    }

    /**
     * Get list of packages with active playback
     */
    fun getPlayingPackages(): List<String> {
        return activeSessions.values
            .filter { it.isPlaying }
            .map { it.packageName }
    }
}
