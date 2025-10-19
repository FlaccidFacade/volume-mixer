package com.volumemixer.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * NotificationListenerService required for accessing media sessions via MediaSessionManager.
 * This service needs to be enabled by the user in Settings > Notification Access.
 */
class MediaNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "MediaNotificationListener"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Not used for media session tracking, but required to override
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Not used for media session tracking, but required to override
    }
}
