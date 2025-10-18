# Volume Mixer Service Architecture

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         VOLUME MIXER APP                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        UI LAYER (Compose)                        │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  MainActivity                                            │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │ VolumeMixerScreen (Composable)                     │ │  │
│  │  │                                                     │ │  │
│  │  │  ┌─────────────────────────────────────┐          │ │  │
│  │  │  │     "Volume Mixer" (Title)          │          │ │  │
│  │  │  └─────────────────────────────────────┘          │ │  │
│  │  │                                                     │ │  │
│  │  │  ┌─────────────────────────────────────┐          │ │  │
│  │  │  │  Service State: Running/Stopped     │          │ │  │
│  │  │  └─────────────────────────────────────┘          │ │  │
│  │  │                                                     │ │  │
│  │  │  ┌─────────────────────────────────────┐          │ │  │
│  │  │  │  [Start/Stop Service] (Button)      │          │ │  │
│  │  │  └─────────────────────────────────────┘          │ │  │
│  │  │         │                                          │ │  │
│  │  │         ├─onStartService()                         │ │  │
│  │  │         └─onStopService()                          │ │  │
│  │  └──────────┬──────────────────────────────────────┬──┘ │  │
│  │             │                                       │    │  │
│  │    startAudioSessionService()        stopAudioSessionService()
│  │             │                                       │    │  │
│  └─────────────┼───────────────────────────────────────┼────┘  │
│                │                                       │        │
└────────────────┼───────────────────────────────────────┼────────┘
                 │                                       │
                 │ Intent(ACTION_START)                  │ Intent(ACTION_STOP)
                 ▼                                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SERVICE LAYER (Foreground)                     │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  AudioSessionService (extends Service)                   │  │
│  │                                                           │  │
│  │  Lifecycle Methods:                                       │  │
│  │  ├─ onCreate()                                            │  │
│  │  │  └─ createNotificationChannel()                       │  │
│  │  │                                                        │  │
│  │  ├─ onStartCommand(intent, flags, startId)               │  │
│  │  │  ├─ ACTION_START → startForeground()                  │  │
│  │  │  │                 └─ Log: "Service running"          │  │
│  │  │  └─ ACTION_STOP → stopSelf()                          │  │
│  │  │                                                        │  │
│  │  └─ onDestroy()                                           │  │
│  │     └─ Log: "Service stopped"                            │  │
│  │                                                           │  │
│  │  Notification:                                            │  │
│  │  ├─ createNotification()                                  │  │
│  │  │  ├─ Title: "Volume Mixer Active"                      │  │
│  │  │  ├─ Text: "Audio session tracking is active"          │  │
│  │  │  ├─ Icon: Media play icon                             │  │
│  │  │  ├─ Action: Open MainActivity                          │  │
│  │  │  └─ Ongoing: true (persistent)                        │  │
│  │  │                                                        │  │
│  │  Stub Functions (Future Implementation):                  │  │
│  │  ├─ trackAudioSessions()                                  │  │
│  │  └─ stopTrackingSessions()                                │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ANDROID SYSTEM SERVICES                       │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  NotificationManager                                      │  │
│  │  ├─ Notification Channel: "volume_mixer_channel"         │  │
│  │  ├─ Importance: LOW                                       │  │
│  │  └─ Shows: "Volume Mixer Active" notification            │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Service Manager (System)                                 │  │
│  │  └─ Manages foreground service lifecycle                  │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Sequence Diagram - Starting Service

```
User              MainActivity           Intent           AudioSessionService      NotificationManager
 │                     │                   │                      │                        │
 │  Tap "Start"        │                   │                      │                        │
 ├────────────────────>│                   │                      │                        │
 │                     │                   │                      │                        │
 │                     │ startAudioSessionService()              │                        │
 │                     ├───────────────────│                      │                        │
 │                     │                   │                      │                        │
 │                     │    Create Intent  │                      │                        │
 │                     │  (ACTION_START)   │                      │                        │
 │                     ├──────────────────>│                      │                        │
 │                     │                   │                      │                        │
 │                     │    startService() │                      │                        │
 │                     │                   ├─────────────────────>│                        │
 │                     │                   │                      │                        │
 │                     │                   │       onCreate()     │                        │
 │                     │                   │       (if new)       │                        │
 │                     │                   │       │              │                        │
 │                     │                   │       └─createNotificationChannel()          │
 │                     │                   │                      ├───────────────────────>│
 │                     │                   │                      │                        │
 │                     │                   │  onStartCommand()    │                        │
 │                     │                   │       │              │                        │
 │                     │                   │  Log: "Service       │                        │
 │                     │                   │       running"       │                        │
 │                     │                   │       │              │                        │
 │                     │                   │  startForeground()   │                        │
 │                     │                   │       │              │                        │
 │                     │                   │       └─createNotification()                 │
 │                     │                   │                      ├───────────────────────>│
 │                     │                   │                      │                        │
 │                     │                   │                      │  Show Notification     │
 │                     │                   │                      │<───────────────────────┤
 │                     │                   │                      │                        │
 │                     │     return        │                      │                        │
 │                     │<──────────────────┤                      │                        │
 │                     │                   │                      │                        │
 │  UI updates         │                   │                      │                        │
 │  "Service running"  │                   │                      │                        │
 │<────────────────────┤                   │                      │                        │
 │                     │                   │                      │                        │
```

## Sequence Diagram - Stopping Service

```
User              MainActivity           Intent           AudioSessionService      NotificationManager
 │                     │                   │                      │                        │
 │  Tap "Stop"         │                   │                      │                        │
 ├────────────────────>│                   │                      │                        │
 │                     │                   │                      │                        │
 │                     │ stopAudioSessionService()               │                        │
 │                     ├───────────────────│                      │                        │
 │                     │                   │                      │                        │
 │                     │    Create Intent  │                      │                        │
 │                     │   (ACTION_STOP)   │                      │                        │
 │                     ├──────────────────>│                      │                        │
 │                     │                   │                      │                        │
 │                     │    startService() │                      │                        │
 │                     │                   ├─────────────────────>│                        │
 │                     │                   │                      │                        │
 │                     │                   │  onStartCommand()    │                        │
 │                     │                   │       │              │                        │
 │                     │                   │    stopSelf()        │                        │
 │                     │                   │       │              │                        │
 │                     │                   │       ▼              │                        │
 │                     │                   │  onDestroy()         │                        │
 │                     │                   │       │              │                        │
 │                     │                   │  Log: "Service       │                        │
 │                     │                   │       stopped"       │                        │
 │                     │                   │       │              │                        │
 │                     │                   │  Notification        │                        │
 │                     │                   │  removed             │                        │
 │                     │                   │  automatically       │                        │
 │                     │                   │                      ├───────────────────────>│
 │                     │                   │                      │                        │
 │                     │                   │                      │  Hide Notification     │
 │                     │                   │                      │<───────────────────────┤
 │                     │     return        │                      │                        │
 │                     │<──────────────────┤                      │                        │
 │                     │                   │                      │                        │
 │  UI updates         │                   │                      │                        │
 │  "Service stopped"  │                   │                      │                        │
 │<────────────────────┤                   │                      │                        │
 │                     │                   │                      │                        │
```

## State Diagram

```
┌─────────────────────┐
│   Service Stopped   │
│  (Initial State)    │
└──────────┬──────────┘
           │
           │ Intent(ACTION_START)
           │
           ▼
┌─────────────────────┐
│   onCreate()        │
│  - Create channel   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  onStartCommand()   │
│  - Log: "Service    │
│         running"    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ startForeground()   │
│  - Show notification│
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Service Running    │◄──┐
│  (Foreground)       │   │
│                     │   │ Multiple
│  - Notification     │   │ START intents
│    visible          │   │ (idempotent)
│  - Service active   │   │
└──────────┬──────────┘   │
           │               │
           │ Intent(ACTION_START)
           ├───────────────┘
           │
           │ Intent(ACTION_STOP)
           │
           ▼
┌─────────────────────┐
│  stopSelf()         │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   onDestroy()       │
│  - Cleanup          │
│  - Log: "Service    │
│         stopped"    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Service Stopped    │
│  - Notification     │
│    removed          │
└─────────────────────┘
```

## Permission Flow (Android 13+)

```
┌────────────────────────────────────────────────────────────┐
│  App Installation                                          │
│  ├─ FOREGROUND_SERVICE (install-time) ✓                   │
│  └─ FOREGROUND_SERVICE_MEDIA_PLAYBACK (install-time) ✓    │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│  First App Launch (Android 13+)                            │
│  └─ POST_NOTIFICATIONS (runtime) ⚠                         │
│     Note: Not implemented yet - future enhancement         │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│  User Action                                                │
│  └─ Tap "Start Service" button                            │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│  Service Start                                              │
│  ├─ Foreground service starts (permission granted)         │
│  └─ Notification shows (if permission granted)             │
└────────────────────────────────────────────────────────────┘
```

## Integration Points (Future)

```
AudioSessionService
        │
        ├──────────────────┐
        │                  │
        ▼                  ▼
AppVolumeDatabase    AudioManager (Android)
        │                  │
        ├─ Read prefs      ├─ Monitor sessions
        ├─ Apply volumes   ├─ Adjust volumes
        └─ Persist changes └─ Track apps
```

## File Structure

```
volume-mixer/
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml ─────────┐
│       │   │   ├─ Permissions               │ Modified
│       │   │   └─ Service declaration       │
│       │   ├── java/com/volumemixer/        │
│       │   │   ├── MainActivity.kt ─────────┤
│       │   │   │   ├─ UI controls           │ Modified
│       │   │   │   └─ Service integration   │
│       │   │   └── service/                 │
│       │   │       └── AudioSessionService.kt ┐ NEW
│       │   │           ├─ Lifecycle          │
│       │   │           ├─ Notification       │
│       │   │           └─ Stub functions     │
│       │   └── res/
│       │       └── values/
│       │           └── strings.xml ──────────┤
│       │               └─ Notification text  │ Modified
│       └── test/
│           └── java/com/volumemixer/
│               └── service/
│                   └── AudioSessionServiceTest.kt ┐ NEW
│                       └─ Basic tests             │
└── FOREGROUND_SERVICE_IMPLEMENTATION.md ──────────┘ NEW
```
