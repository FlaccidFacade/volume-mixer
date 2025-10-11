# Quick Start Guide

## Prerequisites

- **Android Studio** Arctic Fox (2020.3.1) or later
- **JDK** 17
- **Android SDK** with API level 34
- **NDK** 25.0.8775105 or later
- Internet connection (for initial build)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/FlaccidFacade/volume-mixer.git
cd volume-mixer
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select **File → Open**
3. Navigate to the cloned repository
4. Click **OK**

Android Studio will automatically:
- Download the Gradle wrapper
- Sync dependencies
- Configure the NDK

### 3. Build the Project

#### Using Android Studio
- Click **Build → Make Project** (Ctrl+F9 / Cmd+F9)

#### Using Command Line
```bash
./gradlew assembleDebug
```

### 4. Run on Device/Emulator

#### Using Android Studio
- Click **Run → Run 'app'** (Shift+F10)

#### Using Command Line
```bash
./gradlew installDebug
adb shell am start -n com.volumemixer/.MainActivity
```

## Project Structure Overview

```
volume-mixer/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/volumemixer/
│   │   │   │   ├── ui/              # Jetpack Compose UI
│   │   │   │   ├── service/         # Room DB & Services
│   │   │   │   ├── jni/             # JNI Interface
│   │   │   │   └── MainActivity.kt
│   │   │   ├── cpp/                 # Native C++ Code
│   │   │   │   ├── native-lib.cpp
│   │   │   │   └── CMakeLists.txt
│   │   │   └── res/                 # Resources
│   │   ├── test/                    # Unit Tests
│   │   └── androidTest/             # Instrumentation Tests
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Key Components

### 1. MainActivity
The main entry point of the app using Jetpack Compose.

**Location**: `app/src/main/java/com/volumemixer/MainActivity.kt`

### 2. Room Database
Stores per-app volume preferences persistently.

**Files**:
- `AppVolumeDatabase.kt` - Database configuration
- `AppVolumePreference.kt` - Data model
- `AppVolumeDao.kt` - Database operations

**Location**: `app/src/main/java/com/volumemixer/service/`

### 3. JNI/Native Code
Native C++ code for advanced features and root detection.

**Files**:
- `NativeLib.kt` - Kotlin interface
- `native-lib.cpp` - C++ implementation
- `CMakeLists.txt` - Build configuration

**Locations**:
- Kotlin: `app/src/main/java/com/volumemixer/jni/`
- C++: `app/src/main/cpp/`

## Common Tasks

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumentation Tests

```bash
./gradlew connectedAndroidTest
```

### Generate APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Clean Build

```bash
./gradlew clean
```

## Troubleshooting

### Gradle Wrapper Not Found

If you see "gradlew: command not found" or wrapper issues:

```bash
gradle wrapper --gradle-version 8.4
chmod +x gradlew
```

### NDK Not Configured

If NDK is missing:
1. Open Android Studio
2. Go to **Tools → SDK Manager**
3. Select **SDK Tools** tab
4. Check **NDK (Side by side)**
5. Click **Apply**

### Build Fails with Network Errors

Ensure you have internet access to download:
- Android Gradle Plugin from dl.google.com
- Dependencies from Maven Central

If behind a proxy, configure in `gradle.properties`:
```properties
systemProp.http.proxyHost=proxy.example.com
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=proxy.example.com
systemProp.https.proxyPort=8080
```

### Room Compiler Errors

Ensure KSP is properly configured. Clean and rebuild:
```bash
./gradlew clean
./gradlew build
```

## Next Steps

After successful build:

1. **Implement UI Features**
   - Add volume sliders
   - Create app list view
   - Implement settings screen

2. **Add Business Logic**
   - Audio session management
   - Per-app volume control
   - Preference persistence

3. **Enhance Native Code**
   - Implement actual root detection
   - Add audio manipulation features

4. **Testing**
   - Write comprehensive unit tests
   - Add UI tests with Compose Testing
   - Test on various Android versions

## Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Android NDK Documentation](https://developer.android.com/ndk)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)

## Getting Help

- Check [BUILD_NOTES.md](BUILD_NOTES.md) for build status
- See [VALIDATION.md](VALIDATION.md) for implementation details
- Open an issue on GitHub for problems
