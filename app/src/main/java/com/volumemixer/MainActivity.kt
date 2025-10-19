package com.volumemixer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.volumemixer.service.AudioSessionService
import com.volumemixer.ui.VolumeMixerTheme

class MainActivity : ComponentActivity() {
    
    private val activeSessions = mutableStateOf<List<SessionDisplay>>(emptyList())
    
    private val sessionChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioSessionService.ACTION_SESSION_CHANGED) {
                val sessionData = intent.getStringArrayExtra(AudioSessionService.EXTRA_SESSION_DATA)
                activeSessions.value = sessionData?.mapNotNull { data ->
                    // Use lastIndexOf to handle package names with colons
                    val colonIndex = data.lastIndexOf(":")
                    if (colonIndex > 0 && colonIndex < data.length - 1) {
                        val packageName = data.substring(0, colonIndex)
                        val playingStr = data.substring(colonIndex + 1)
                        SessionDisplay(
                            packageName = packageName,
                            isPlaying = playingStr == "true"
                        )
                    } else {
                        null // Skip malformed entries
                    }
                } ?: emptyList()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register broadcast receiver
        val filter = IntentFilter(AudioSessionService.ACTION_SESSION_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(sessionChangeReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(sessionChangeReceiver, filter)
        }
        
        setContent {
            VolumeMixerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VolumeMixerScreen(
                        activeSessions = activeSessions.value,
                        onStartService = { startAudioSessionService() },
                        onStopService = { stopAudioSessionService() }
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(sessionChangeReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered, ignore
        }
    }

    private fun startAudioSessionService() {
        val intent = Intent(this, AudioSessionService::class.java).apply {
            action = AudioSessionService.ACTION_START
        }
        startService(intent)
    }

    private fun stopAudioSessionService() {
        val intent = Intent(this, AudioSessionService::class.java).apply {
            action = AudioSessionService.ACTION_STOP
        }
        startService(intent)
    }
    
}

/**
 * Data class representing a media session for display in the UI.
 */
data class SessionDisplay(
    val packageName: String,
    val isPlaying: Boolean
)

@Composable
fun VolumeMixerScreen(
    activeSessions: List<SessionDisplay> = emptyList(),
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    var isServiceRunning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Volume Mixer",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = if (isServiceRunning) "Service is running" else "Service is stopped",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Button(
            onClick = {
                if (isServiceRunning) {
                    onStopService()
                    isServiceRunning = false
                } else {
                    onStartService()
                    isServiceRunning = true
                }
            }
        ) {
            Text(text = if (isServiceRunning) "Stop Service" else "Start Service")
        }
        
        // Active Sessions Section
        Text(
            text = "Active Media Sessions",
            style = MaterialTheme.typography.titleLarge
        )
        
        if (activeSessions.isEmpty()) {
            Text(
                text = "No active media sessions detected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeSessions.forEach { session ->
                    SessionCard(session)
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: SessionDisplay) {
    Card(
        modifier = Modifier.padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (session.isPlaying) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = session.packageName,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (session.isPlaying) "▶ Playing" else "⏸ Paused",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VolumeMixerScreenPreview() {
    VolumeMixerTheme {
        VolumeMixerScreen(
            activeSessions = listOf(
                SessionDisplay("com.spotify.music", true),
                SessionDisplay("com.google.android.youtube", false)
            ),
            onStartService = {},
            onStopService = {}
        )
    }
}
