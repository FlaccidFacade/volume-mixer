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

- **Jetpack Compose UI**: Modern declarative UI framework
- **Room Database**: SQLite abstraction for storing app volume preferences
- **NDK/JNI Support**: Native C++ integration for advanced audio control
- **Root Detection**: Stub implementation for Magisk root detection

## Requirements

- Android Studio Arctic Fox or later
- Android SDK 34
- NDK 25.0.8775105 or later
- Gradle 8.4+
- JDK 17

## Building

```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test
```

## Setup Notes

The project requires internet access during initial build to download dependencies from:
- Google's Maven repository (for Android Gradle Plugin and AndroidX libraries)
- Maven Central (for Kotlin and other dependencies)

If you're in a restricted environment, you may need to configure proxy settings in `gradle.properties`.
