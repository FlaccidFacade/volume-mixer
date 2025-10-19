# Testing Guide - Active Media Session Detection (Issue #3)

## Quick Start

### Prerequisites
- Android device or emulator with API 26+ (Android 8.0+)
- ADB installed and configured
- At least one media app installed (Spotify, YouTube, Google Play Music, etc.)

### Setup Steps

1. **Install the App**
   ```bash
   cd /home/runner/work/volume-mixer/volume-mixer
   ./gradlew installDebug
   ```

2. **Enable Notification Access** (REQUIRED)
   - Open device Settings
   - Navigate to: Apps → Volume Mixer → Notification Access
   - OR: Settings → Notification → Notification Access
   - Enable "Volume Mixer"
   - This grants permission to access media sessions

3. **Start the Service**
   ```bash
   adb shell am start -n com.volumemixer/.MainActivity
   ```
   - Tap "Start Service" in the app

4. **Verify Setup**
   ```bash
   # Check if notification access is granted
   adb shell cmd notification allow_listener com.volumemixer/.service.MediaNotificationListenerService
   
   # Check service is running
   adb shell dumpsys activity services | grep AudioSessionService
   ```

## Manual Test Cases

### Test 1: Service Starts with Session Tracking

**Objective:** Verify that media session tracking starts with the service

**Steps:**
1. Clear logcat: `adb logcat -c`
2. Open Volume Mixer app
3. Tap "Start Service"
4. Check logcat: `adb logcat -s AudioSessionService:D MediaSessionTracker:D`

**Expected Results:**
```
✅ See "Service starting"
✅ See "Media session tracking started"
✅ See "Started tracking media sessions"
✅ No crash or error messages
```

**Logcat Output:**
```
D/AudioSessionService: Service starting
D/AudioSessionService: Media session tracking started
D/MediaSessionTracker: Started tracking media sessions
```

---

### Test 2: Detect Single Active Session

**Objective:** Verify detection when one app starts playing

**Steps:**
1. Start Volume Mixer service
2. Clear logcat
3. Open Spotify (or any media app)
4. Play music
5. Check logcat
6. Check Volume Mixer UI

**Expected Results:**
```
✅ Logcat shows "Active sessions changed, count: 1"
✅ Logcat shows package name (e.g., "com.spotify.music")
✅ Logcat shows "Playback State: PLAYING"
✅ Logcat shows "Is Playing: true"
✅ UI shows session card with package name
✅ UI shows "▶ Playing" indicator
✅ Card is highlighted (primary container color)
```

**Logcat Output:**
```
D/MediaSessionTracker: Active sessions changed, count: 1
D/AudioSessionService: === Active Media Sessions ===
D/AudioSessionService: Package: com.spotify.music
D/AudioSessionService:   Session ID: android.media.session.MediaSessionToken@a1b2c3d4
D/AudioSessionService:   Playback State: PLAYING
D/AudioSessionService:   Is Playing: true
D/AudioSessionService: =============================
```

**UI Screenshot:**
- Should show card with "com.spotify.music" and "▶ Playing"

---

### Test 3: Detect Playback State Changes

**Objective:** Verify state changes are detected (playing → paused → playing)

**Steps:**
1. Have music playing (from Test 2)
2. Pause the music
3. Check logcat and UI
4. Resume playback
5. Check logcat and UI again

**Expected Results (Pause):**
```
✅ Logcat shows "Playback State: PAUSED"
✅ Logcat shows "Is Playing: false"
✅ UI updates to "⏸ Paused"
✅ Card color changes to surface variant
```

**Expected Results (Resume):**
```
✅ Logcat shows "Playback State: PLAYING"
✅ Logcat shows "Is Playing: true"
✅ UI updates to "▶ Playing"
✅ Card color changes back to primary container
```

**Logcat Output (Paused):**
```
D/MediaSessionTracker: Active sessions changed, count: 1
D/AudioSessionService: Package: com.spotify.music
D/AudioSessionService:   Playback State: PAUSED
D/AudioSessionService:   Is Playing: false
```

---

### Test 4: Detect Multiple Active Sessions

**Objective:** Verify multiple apps can be tracked simultaneously

**Steps:**
1. Start Spotify and play music
2. Keep it playing
3. Open YouTube app
4. Play a video
5. Check logcat and UI

**Expected Results:**
```
✅ Logcat shows "count: 2"
✅ Both package names appear in logs
✅ UI shows two session cards
✅ Both cards show correct playback states
✅ Order may vary
```

**Logcat Output:**
```
D/MediaSessionTracker: Active sessions changed, count: 2
D/AudioSessionService: === Active Media Sessions ===
D/AudioSessionService: Package: com.spotify.music
D/AudioSessionService:   Playback State: PLAYING
D/AudioSessionService:   Is Playing: true
D/AudioSessionService: Package: com.google.android.youtube
D/AudioSessionService:   Playback State: PLAYING
D/AudioSessionService:   Is Playing: true
D/AudioSessionService: =============================
```

**UI Screenshot:**
- Should show two cards, both marked as playing

---

### Test 5: Session Removal Detection

**Objective:** Verify sessions are removed when playback stops

**Steps:**
1. Have music playing
2. Stop playback completely (not just pause)
3. Close the media app
4. Wait 5 seconds
5. Check logcat and UI

**Expected Results:**
```
✅ Logcat shows "Session removed: [package name]"
✅ Session count decreases
✅ UI removes the session card
✅ If no sessions remain, UI shows "No active media sessions detected"
```

**Logcat Output:**
```
D/MediaSessionTracker: Session removed: com.spotify.music
D/MediaSessionTracker: Active sessions changed, count: 0
D/AudioSessionService: === Active Media Sessions ===
D/AudioSessionService: No active media sessions
D/AudioSessionService: =============================
```

---

### Test 6: Service Lifecycle Management

**Objective:** Verify proper cleanup when service stops

**Steps:**
1. Have active media sessions
2. Tap "Stop Service" in app
3. Check logcat

**Expected Results:**
```
✅ Logcat shows "Service stopping"
✅ Logcat shows "Media session tracking stopped"
✅ Service notification disappears
✅ No crash or errors
```

**Logcat Output:**
```
D/AudioSessionService: Service stopping
D/AudioSessionService: Media session tracking stopped
D/MediaSessionTracker: Stopped tracking media sessions
D/AudioSessionService: Service destroyed
```

---

### Test 7: Service Survives App Background

**Objective:** Verify tracking continues when app is backgrounded

**Steps:**
1. Start service with music playing
2. Verify session appears in UI
3. Press Home button
4. Wait 10 seconds
5. Check notification shade for service notification
6. Change playback state (pause/play)
7. Return to app

**Expected Results:**
```
✅ Service notification remains visible
✅ Tracking continues in background (check logcat)
✅ UI updates when app returns to foreground
✅ Session state is current
```

---

### Test 8: Handle Missing Notification Access

**Objective:** Verify graceful handling when permission not granted

**Steps:**
1. Disable notification access in Settings
2. Start Volume Mixer service
3. Play music in another app
4. Check logcat

**Expected Results:**
```
✅ Service starts without crash
✅ Logcat shows "SecurityException: Missing notification listener permission"
✅ Logcat shows helpful message about enabling access
✅ No sessions detected (expected behavior)
```

**Logcat Output:**
```
D/AudioSessionService: Service starting
D/AudioSessionService: Media session tracking started
E/MediaSessionTracker: SecurityException: Missing notification listener permission. Please enable notification access in Settings > Notification Access
```

---

### Test 9: UI Broadcast Reception

**Objective:** Verify UI receives and displays broadcasts correctly

**Steps:**
1. Start service
2. Open another app over Volume Mixer
3. Play music
4. Switch back to Volume Mixer
5. Verify UI updated

**Expected Results:**
```
✅ UI shows active session
✅ Broadcast was received while in background
✅ State is current
```

---

### Test 10: Package Name with Special Characters

**Objective:** Test edge case of package names with colons

**Steps:**
1. If available, test with app that has colon in package name
2. OR verify parsing logic with manual test:
   ```bash
   # Send test broadcast
   adb shell am broadcast -a com.volumemixer.action.SESSION_CHANGED \
     --esa com.volumemixer.extra.SESSION_DATA "com.test:app:true"
   ```
3. Check UI and logs

**Expected Results:**
```
✅ Package name parsed correctly as "com.test:app"
✅ Playing state parsed as true
✅ No parsing errors in logcat
```

---

## Automated Test Execution

### Run Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run only MediaSessionTracker tests
./gradlew test --tests MediaSessionTrackerTest

# Run only AudioSessionService tests
./gradlew test --tests AudioSessionServiceTest

# Run with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

**Expected Results:**
```
✅ All 21 tests pass
✅ No test failures
✅ Coverage report generated
```

### Test Output Example
```
MediaSessionTrackerTest
  ✓ mediaSessionInfo_createsCorrectly
  ✓ mediaSessionInfo_identifiesPlayingState
  ✓ mediaSessionInfo_handlesMultipleStates
  ✓ getActiveSessions_returnsEmptyMapInitially
  ✓ hasActiveSession_checksPackageName
  ✓ getPlayingPackages_filtersCorrectly
  ✓ sessionChangeListener_interfaceExists
  ✓ sessionMap_maintainsPackageToInfoMapping
  ✓ sessionMap_updatesExistingPackage
  ✓ sessionMap_removesPackagesNotInUpdate
  ✓ playbackStates_allStatesHandled
  ✓ concurrentHashMap_supportsThreadSafeOperations

AudioSessionServiceTest
  ✓ serviceActions_areDefinedCorrectly
  ✓ serviceConstants_areNotNull
  ✓ sessionChangedAction_hasCorrectValue
  ✓ sessionDataExtra_hasCorrectValue
  ✓ broadcastIntentFormat_parsesCorrectly
  ✓ broadcastIntentFormat_handlesColonInPackageName
  ✓ broadcastIntentFormat_validatesBooleanString

BUILD SUCCESSFUL
```

---

## Performance Testing

### Memory Usage

**Test:**
```bash
# Start service with multiple sessions
# Monitor memory
adb shell dumpsys meminfo com.volumemixer
```

**Expected Results:**
```
✅ Native Heap: <20 MB
✅ Java Heap: <30 MB
✅ Total PSS: <50 MB
✅ No memory leaks over time
```

### Battery Usage

**Test:**
1. Start service
2. Keep multiple sessions active
3. Run for 1 hour
4. Check battery stats

**Expected Results:**
```
✅ Battery usage <1% per hour
✅ No excessive wake locks
✅ CPU usage minimal
```

### Callback Frequency

**Test:**
1. Play/pause rapidly
2. Monitor logcat for callback frequency

**Expected Results:**
```
✅ Callbacks trigger only on actual state changes
✅ No excessive callback storms
✅ Reasonable debouncing
```

---

## Debugging Common Issues

### Issue: No Sessions Detected

**Symptoms:**
- UI shows "No active media sessions detected"
- Music is playing but not detected

**Debug Steps:**
```bash
# 1. Check notification access
adb shell cmd notification allow_listener com.volumemixer/.service.MediaNotificationListenerService

# 2. Check service status
adb shell dumpsys notification | grep -A 10 volumemixer

# 3. Check for SecurityException
adb logcat -s MediaSessionTracker:E

# 4. Restart notification listener
adb shell cmd notification disallow_listener com.volumemixer/.service.MediaNotificationListenerService
adb shell cmd notification allow_listener com.volumemixer/.service.MediaNotificationListenerService
```

**Solutions:**
- Enable notification access in Settings
- Restart the app
- Restart the device

---

### Issue: UI Not Updating

**Symptoms:**
- Sessions detected in logcat
- UI not showing sessions

**Debug Steps:**
```bash
# 1. Check broadcast is sent
adb logcat | grep ACTION_SESSION_CHANGED

# 2. Check receiver registered
adb shell dumpsys activity broadcasts | grep volumemixer

# 3. Force broadcast manually
adb shell am broadcast -a com.volumemixer.action.SESSION_CHANGED \
  --esa com.volumemixer.extra.SESSION_DATA "com.test:true"
```

**Solutions:**
- Ensure app is in foreground
- Check receiver registration in MainActivity
- Verify broadcast action string matches

---

### Issue: Service Crashes

**Symptoms:**
- Service stops unexpectedly
- Notification disappears

**Debug Steps:**
```bash
# 1. Check crash logs
adb logcat -s AndroidRuntime:E

# 2. Check MediaSessionManager errors
adb logcat -s MediaSessionManager:E

# 3. Full stack trace
adb logcat *:E
```

**Solutions:**
- Check notification listener permission
- Verify ComponentName is correct
- Check for null pointer exceptions

---

## Test Results Template

Use this template to document test session results:

```markdown
## Test Session Results

**Date:** [Date]
**Tester:** [Name]
**Device:** [Model]
**Android Version:** [API Level]
**Build:** Debug/Release

### Environment Setup
- [ ] App installed successfully
- [ ] Notification access enabled
- [ ] Service started
- [ ] Logcat monitoring active

### Test Results

| Test Case | Status | Notes |
|-----------|--------|-------|
| 1. Service Starts | ✅/❌ | |
| 2. Single Session | ✅/❌ | |
| 3. State Changes | ✅/❌ | |
| 4. Multiple Sessions | ✅/❌ | |
| 5. Session Removal | ✅/❌ | |
| 6. Service Lifecycle | ✅/❌ | |
| 7. App Background | ✅/❌ | |
| 8. Missing Permission | ✅/❌ | |
| 9. Broadcast Reception | ✅/❌ | |
| 10. Special Characters | ✅/❌ | |

### Unit Tests
- [ ] All 21 tests passed
- [ ] No test failures
- [ ] Coverage: ____%

### Performance
- Memory Usage: ___ MB
- Battery Impact: ___% per hour
- No memory leaks: ✅/❌

### Issues Found
[List any issues or unexpected behavior]

### Screenshots
[Attach screenshots of UI with active sessions]

### Overall Result
✅ PASS / ❌ FAIL

### Additional Notes
[Any other observations]
```

---

## Known Media Apps for Testing

### Recommended Apps
- **Spotify** - Excellent MediaSession support
- **YouTube** - Good support
- **Google Play Music** - Good support
- **VLC** - Good support
- **PowerAmp** - Good support

### Apps with Limitations
- Some browsers (Chrome, Firefox) - May not report sessions
- Some game audio - Not using MediaSession API
- System sounds - Not tracked as media sessions

---

## Continuous Integration Testing

### GitHub Actions Workflow (Future)

```yaml
name: Test Media Session Detection

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run unit tests
        run: ./gradlew test
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

---

## Success Criteria

### For Release
- ✅ All 10 manual test cases pass
- ✅ All 21 unit tests pass
- ✅ No crashes or ANRs
- ✅ Memory usage within limits
- ✅ Battery impact <1% per hour
- ✅ UI updates reliably
- ✅ Handles edge cases gracefully

### Quality Gates
- ✅ Code reviewed
- ✅ Documentation complete
- ✅ All critical bugs fixed
- ✅ Performance acceptable
- ✅ User experience smooth

---

## Next Steps After Testing

1. Document any issues found
2. Create bug reports for failures
3. Update implementation if needed
4. Plan instrumentation tests
5. Consider adding UI tests with Compose Testing
6. Plan integration with volume control (Issue #4)

---

**Last Updated:** October 19, 2025
**Status:** Ready for Testing
**Version:** 1.0.0
