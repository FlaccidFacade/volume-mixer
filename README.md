# Volume Mixer

An Android application for per-app volume control using Kotlin and Jetpack Compose.

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/volumemixer/
│   │   │   ├── ui/          # UI components and themes
│   │   │   ├── service/     # Room database and services
│   │   │   ├── jni/         # JNI/Native interface
│   │   │   └── MainActivity.kt
│   │   ├── cpp/             # Native C++ code for JNI
│   │   └── res/             # Android resources
│   ├── test/                # Unit tests
│   └── androidTest/         # Instrumentation tests
```

## Features

- **Jetpack Compose UI**: Modern declarative UI framework with Material3 design
- **Room Database**: SQLite abstraction for storing app volume preferences
- **Active Media Session Detection**: Real-time tracking of apps playing audio using MediaSessionManager
- **Foreground Service**: Persistent background service for continuous audio monitoring
- **NDK/JNI Support**: Native C++ integration for advanced audio control
- **Root Detection**: Stub implementation for Magisk root detection

### Recent Additions (Issue #3)

**Active Media Session Detection**:
- Real-time detection of apps playing audio
- Thread-safe session tracking with package name → session ID mapping
- Live UI updates showing active media sessions
- Visual indicators for playing/paused states
- Comprehensive logging of all session changes

For detailed information, see:
- `IMPLEMENTATION_NOTES_ISSUE_3.md` - Technical details and architecture
- `TESTING_GUIDE_ISSUE_3.md` - Testing procedures
- `ISSUE_3_COMPLETION_SUMMARY.md` - Implementation summary

## Requirements

- Android Studio Arctic Fox or later
- Android SDK 34 (minimum SDK 26)
- NDK 25.0.8775105 or later
- Gradle 8.4+
- JDK 17
- **Notification Access Permission** (required for media session detection)

## Building

```bash
./gradlew assembleDebug
```

## Testing

### Unit Tests
```bash
./gradlew test
```

### Manual Testing
See `TESTING_GUIDE_ISSUE_3.md` for detailed test cases and procedures.

**Quick Test:**
1. Install app: `./gradlew installDebug`
2. Enable notification access in Settings → Apps → Volume Mixer → Notification Access
3. Start the service in the app
4. Play music in any app (Spotify, YouTube, etc.)
5. View active sessions in Volume Mixer UI

## Setup Notes

The project requires internet access during initial build to download dependencies from:
- Google's Maven repository (for Android Gradle Plugin and AndroidX libraries)
- Maven Central (for Kotlin and other dependencies)

If you're in a restricted environment, you may need to configure proxy settings in `gradle.properties`.

### First-Time Setup

After installing the app:
1. Open device Settings
2. Navigate to Apps → Volume Mixer → Notification Access
3. Enable notification access (required for media session detection)
4. Return to Volume Mixer app
5. Tap "Start Service"
6. The app will now detect active media sessions

## Documentation

- `ARCHITECTURE.md` - System architecture overview
- `IMPLEMENTATION_NOTES_ISSUE_2.md` - Foreground service implementation
- `IMPLEMENTATION_NOTES_ISSUE_3.md` - Media session detection implementation
- `TESTING_GUIDE_ISSUE_3.md` - Testing procedures for media session detection
- `ISSUE_3_COMPLETION_SUMMARY.md` - Feature completion summary

## License

[License information to be added]
