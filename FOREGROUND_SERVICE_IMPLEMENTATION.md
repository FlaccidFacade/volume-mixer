# Foreground Service Implementation

## Overview
This document describes the implementation of the foreground service for audio session tracking in the Volume Mixer application.

## Implementation Summary

### Components Created

#### 1. AudioSessionService (`app/src/main/java/com/volumemixer/service/AudioSessionService.kt`)
A foreground service that keeps the app alive and displays a persistent notification.

**Key Features:**
- Extends Android's `Service` class
- Runs as a foreground service with persistent notification
- Implements proper lifecycle methods (onCreate, onStartCommand, onDestroy)
- Includes stub functions for future audio session tracking implementation
- Logs "Service running" when started
- Uses START_STICKY return policy to ensure service restarts if killed

**Service Actions:**
- `ACTION_START`: Starts the foreground service with notification
- `ACTION_STOP`: Stops the service

**Notification:**
- Title: "Volume Mixer Active"
- Text: "Audio session tracking is active"
- Icon: Standard Android media play icon
- Importance: LOW (doesn't make sound or vibrate)
- Taps notification to open MainActivity
- Persistent (ongoing) notification that cannot be dismissed while service runs

**Stub Functions:**
- `trackAudioSessions()`: Placeholder for future audio session tracking
- `stopTrackingSessions()`: Placeholder for stopping audio session tracking

#### 2. MainActivity Updates
Enhanced the MainActivity to provide UI controls for the service.

**Changes:**
- Added start/stop service button
- Visual indicator showing service state
- Intent-based service communication
- Compose UI with state management

**UI Layout:**
```
┌─────────────────────────┐
│   Volume Mixer          │  (Headline)
│                         │
│   Service is stopped    │  (Status text)
│   [Start Service]       │  (Button)
└─────────────────────────┘
```

#### 3. AndroidManifest.xml Updates
Added necessary permissions and service registration.

**Permissions Added:**
- `FOREGROUND_SERVICE`: Required for all foreground services
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Required for Android 14+ media-related services
- `POST_NOTIFICATIONS`: Required for Android 13+ to show notifications

**Service Registration:**
```xml
<service
    android:name=".service.AudioSessionService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback" />
```

#### 4. String Resources
Added notification strings to `strings.xml`:
- `notification_title`: "Volume Mixer Active"
- `notification_text`: "Audio session tracking is active"
- `service_start`: "Start Service"
- `service_stop`: "Stop Service"

#### 5. Unit Tests
Created basic unit tests in `AudioSessionServiceTest.kt` to verify:
- Service action constants are defined correctly
- Service constants are not null

Note: Full service testing requires instrumentation tests with an Android device/emulator.

## Architecture

### Service Lifecycle
```
MainActivity → Intent(ACTION_START) → AudioSessionService
                                     ↓
                              onCreate() - Create notification channel
                                     ↓
                              onStartCommand() - Start foreground with notification
                                     ↓
                              Log: "Service running"
                                     ↓
                              [Service Running]
                                     ↓
MainActivity → Intent(ACTION_STOP) → onStartCommand() - stopSelf()
                                     ↓
                              onDestroy() - Cleanup
```

### Notification Channel
The service creates a notification channel on Android O+ with:
- ID: `volume_mixer_channel`
- Name: `Volume Mixer`
- Importance: LOW (no sound/vibration)
- Description: "Persistent notification for Volume Mixer service"

## Testing Requirements

### Manual Testing Checklist
- [ ] Service starts when "Start Service" button is pressed
- [ ] Notification displays with title "Volume Mixer Active"
- [ ] Notification is persistent (cannot be dismissed)
- [ ] Tapping notification opens MainActivity
- [ ] Service stops when "Stop Service" button is pressed
- [ ] Notification disappears when service stops
- [ ] Check logcat for "Service running" message on start
- [ ] Check logcat for "Service created" and "Service stopped" messages
- [ ] Service survives app being put in background
- [ ] Service survives screen rotation

### Logcat Messages to Verify
```
D/AudioSessionService: Service created
D/AudioSessionService: Service running
D/AudioSessionService: Service stopped
```

## Future Enhancements

### Planned Implementations
1. **Audio Session Tracking**
   - Implement `trackAudioSessions()` method
   - Monitor active audio sessions across apps
   - Detect when apps start/stop playing audio

2. **Volume Control**
   - Apply per-app volume preferences from Room database
   - Real-time volume adjustment based on stored preferences

3. **Service Binding**
   - Convert to bound service for two-way communication
   - Expose callbacks to MainActivity
   - Real-time status updates

4. **Enhanced Notification**
   - Add notification actions (e.g., quick volume controls)
   - Update notification with current audio session count
   - Show currently playing apps

5. **Service Manager Class**
   - Create ServiceManager to handle service lifecycle
   - Centralized service state management
   - Better integration with ViewModel pattern

## Technical Notes

### Android Version Compatibility
- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **Notification Channels**: Required for Android O+ (SDK 26+)
- **Foreground Service Types**: Required for Android 14+ (SDK 34+)
- **Runtime Notification Permission**: Required for Android 13+ (SDK 33+)

### Service Type
The service is declared with `foregroundServiceType="mediaPlayback"` because:
- It will be used for audio-related functionality
- Allows access to audio session information
- Appropriate for volume mixing use case

### Permission Model
- Foreground service permissions are install-time permissions (no runtime prompt)
- POST_NOTIFICATIONS requires runtime permission request on Android 13+
- Current implementation does not include runtime permission request UI (future enhancement)

## Code Quality

### Design Patterns Used
1. **Singleton Pattern**: Service instance managed by Android system
2. **Observer Pattern**: Future implementation will use callbacks
3. **Command Pattern**: Service actions (START/STOP) as intent actions

### Best Practices Followed
- Proper logging for debugging
- Separate concerns (UI, Service, Data)
- Constants defined in companion object
- Immutable PendingIntent flags
- Notification channel for Android O+
- Proper service lifecycle management
- START_STICKY for service resilience

## Dependencies
No new dependencies were added. The implementation uses:
- AndroidX Core KTX
- Jetpack Compose
- Android SDK components (Service, Notification, etc.)

## File Changes Summary
```
Modified:
- app/src/main/AndroidManifest.xml (added permissions and service)
- app/src/main/java/com/volumemixer/MainActivity.kt (added UI controls)
- app/src/main/res/values/strings.xml (added notification strings)

Created:
- app/src/main/java/com/volumemixer/service/AudioSessionService.kt
- app/src/test/java/com/volumemixer/service/AudioSessionServiceTest.kt
```

## Known Limitations
1. Service state is not persisted across app restarts (UI state resets)
2. No runtime permission request for POST_NOTIFICATIONS on Android 13+
3. Stub functions for audio tracking not yet implemented
4. No bound service interface for real-time communication
5. Basic notification without action buttons

## Next Steps
1. Add runtime permission request for notifications
2. Implement actual audio session tracking
3. Connect service to Room database for volume preferences
4. Add service state persistence
5. Create instrumentation tests for full service testing
6. Enhance notification with quick actions
