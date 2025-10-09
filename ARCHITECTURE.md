# Architecture Overview

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         VOLUME MIXER APP                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                          PRESENTATION                            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  MainActivity.kt                                         │  │
│  │  - Jetpack Compose Entry Point                          │  │
│  │  - Material3 Theme                                       │  │
│  │  - PlaceholderScreen()                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  ui/Theme.kt                                             │  │
│  │  - Dark/Light Color Schemes                             │  │
│  │  - Material3 Configuration                               │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                          DATA LAYER                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  service/AppVolumeDatabase.kt                            │  │
│  │  - Room Database Singleton                               │  │
│  │  - Thread-safe Initialization                            │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  service/AppVolumeDao.kt                                 │  │
│  │  - getAllPreferences() : Flow<List<AppVolumePreference>> │  │
│  │  - getPreference(packageName) : AppVolumePreference?     │  │
│  │  - insertOrUpdate(preference)                            │  │
│  │  - delete(packageName)                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  service/AppVolumePreference.kt                          │  │
│  │  - Entity: packageName, volumeLevel, isMuted             │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                        NATIVE LAYER                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  jni/NativeLib.kt (Kotlin Interface)                     │  │
│  │  - loadLibrary("volumemixer")                            │  │
│  │  - external fun isRooted(): Boolean                      │  │
│  │  - external fun getVersion(): String                     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ↓ JNI                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  cpp/native-lib.cpp (C++ Implementation)                 │  │
│  │  - Java_com_volumemixer_jni_NativeLib_isRooted()        │  │
│  │  - Java_com_volumemixer_jni_NativeLib_getVersion()      │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  cpp/CMakeLists.txt                                      │  │
│  │  - CMake Build Configuration                             │  │
│  │  - Links liblog                                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

### Presentation Layer
**Location**: `app/src/main/java/com/volumemixer/` and `ui/`

- **MainActivity**: Entry point, hosts Compose UI
- **Theme**: Material3 theme configuration with dark/light mode support
- **Composables**: UI components (currently PlaceholderScreen)

**Technologies**:
- Jetpack Compose
- Material3 Design System
- Kotlin Coroutines (via Compose)

### Data Layer
**Location**: `app/src/main/java/com/volumemixer/service/`

- **AppVolumeDatabase**: Room database singleton, manages database lifecycle
- **AppVolumeDao**: Data Access Object, provides CRUD operations
- **AppVolumePreference**: Entity class representing volume preferences

**Technologies**:
- Room Database
- Kotlin Flow for reactive data
- Coroutines for async operations

### Native Layer
**Location**: `app/src/main/java/com/volumemixer/jni/` and `app/src/main/cpp/`

- **NativeLib.kt**: JNI interface, loads native library
- **native-lib.cpp**: C++ implementation of native functions
- **CMakeLists.txt**: Build configuration for native code

**Technologies**:
- JNI (Java Native Interface)
- C++17
- CMake

## Data Flow

### Reading Volume Preferences
```
MainActivity → AppVolumeDao.getAllPreferences()
     ↓
Flow<List<AppVolumePreference>>
     ↓
Compose UI (reacts to changes)
```

### Saving Volume Preferences
```
User Input → ViewModel (future)
     ↓
AppVolumeDao.insertOrUpdate()
     ↓
Room Database
     ↓
Flow updates UI automatically
```

### Root Detection
```
UI calls → NativeLib.isRooted()
     ↓
JNI Bridge
     ↓
native-lib.cpp checks system
     ↓
Boolean result returned to Kotlin
```

## Build Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  Root Project (build.gradle.kts)                                │
│  - Android Gradle Plugin 7.4.2                                  │
│  - Kotlin Plugin 1.8.22                                         │
│  - KSP Plugin 1.8.22-1.0.11                                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  App Module (app/build.gradle.kts)                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Android Configuration                                   │  │
│  │  - compileSdk: 34                                        │  │
│  │  - minSdk: 26                                            │  │
│  │  - targetSdk: 34                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Compose Configuration                                   │  │
│  │  - BOM: 2023.10.01                                       │  │
│  │  - Compiler: 1.4.8                                       │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Room Configuration                                      │  │
│  │  - Runtime: 2.6.1                                        │  │
│  │  - KSP Annotation Processing                             │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  NDK Configuration                                       │  │
│  │  - CMake 3.22.1                                          │  │
│  │  - ABIs: arm64-v8a, armeabi-v7a, x86, x86_64           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│  Native Build (CMakeLists.txt)                                  │
│  - Compiles native-lib.cpp                                      │
│  - Links liblog                                                 │
│  - Generates libvolumemixer.so for each ABI                     │
└─────────────────────────────────────────────────────────────────┘
```

## Threading Model

### UI Thread (Main)
- Compose UI rendering
- User interactions
- State updates

### Background (Coroutines)
- Room database operations (suspend functions)
- Flow collections
- Heavy computations

### Native Thread
- JNI calls execute on calling thread
- Can create native threads if needed

## Testing Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  Unit Tests (app/src/test/)                                     │
│  - JUnit 4.13.2                                                 │
│  - No Android dependencies                                      │
│  - Fast execution on JVM                                        │
│  Example: ExampleUnitTest.kt                                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Instrumentation Tests (app/src/androidTest/)                   │
│  - AndroidX Test Framework                                      │
│  - Espresso 3.5.1                                              │
│  - Compose UI Testing                                           │
│  - Requires Android device/emulator                             │
└─────────────────────────────────────────────────────────────────┘
```

## Dependency Graph

```
MainActivity
    ├── Jetpack Compose
    │   ├── androidx.compose.ui
    │   ├── androidx.compose.material3
    │   └── androidx.activity.compose
    ├── VolumeMixerTheme
    └── PlaceholderScreen

AppVolumeDatabase
    ├── Room Runtime
    ├── Room KTX
    └── AppVolumeDao
        └── AppVolumePreference

NativeLib (Kotlin)
    └── libvolumemixer.so (C++)
        └── liblog.so (Android)
```

## Future Architecture Considerations

### ViewModel Layer (Planned)
```
MainActivity → ViewModel → Repository → DAO/DataSource
```

### Repository Pattern (Planned)
```
VolumeRepository
    ├── LocalDataSource (Room)
    └── AudioManager Integration
```

### Use Cases / Interactors (Planned)
```
GetAllAppsUseCase
SetAppVolumeUseCase
CheckRootAccessUseCase
```

## Design Patterns Used

1. **Singleton Pattern**: AppVolumeDatabase
2. **DAO Pattern**: AppVolumeDao
3. **Observer Pattern**: Kotlin Flow for reactive data
4. **Builder Pattern**: Room database builder
5. **Bridge Pattern**: JNI interface
6. **Composition Pattern**: Jetpack Compose UI

## Security Considerations

1. **Root Detection**: Native implementation reduces tampering
2. **Data Encryption**: Room supports encrypted databases (future)
3. **JNI Obfuscation**: ProGuard rules configured
4. **Input Validation**: Type-safe Kotlin + Room constraints

## Performance Optimizations

1. **Lazy Loading**: Database singleton with synchronized initialization
2. **Coroutines**: Non-blocking database operations
3. **Flow**: Efficient reactive data updates
4. **Compose**: Efficient recomposition with state
5. **Multi-ABI**: Native library for all architectures

## Scalability

The architecture supports:
- Adding more entities to Room database
- Expanding native functionality
- Adding new Compose screens
- Implementing ViewModel layer
- Adding dependency injection (Hilt/Koin)
- Modularization for large codebase

---

**Status**: Initial architecture implementation complete
**Version**: 1.0.0
**Last Updated**: [Current Date]
