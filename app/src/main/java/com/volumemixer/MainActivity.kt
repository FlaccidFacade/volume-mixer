package com.volumemixer

import android.content.Intent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.volumemixer.service.AudioSessionService
import com.volumemixer.ui.VolumeMixerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolumeMixerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VolumeMixerScreen(
                        onStartService = { startAudioSessionService() },
                        onStopService = { stopAudioSessionService() }
                    )
                }
            }
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

@Composable
fun VolumeMixerScreen(
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    var isServiceRunning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Volume Mixer",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = if (isServiceRunning) "Service is running" else "Service is stopped",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
    }
}

@Preview(showBackground = true)
@Composable
fun VolumeMixerScreenPreview() {
    VolumeMixerTheme {
        VolumeMixerScreen(
            onStartService = {},
            onStopService = {}
        )
    }
}
