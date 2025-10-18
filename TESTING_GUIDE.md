# Testing Guide - Foreground Service Implementation

## Quick Start Testing

### Prerequisites
- Android device or emulator with API 26+ (Android 8.0 Oreo or higher)
- USB debugging enabled (for physical device)
- ADB installed and configured

### Installation Steps

1. **Build the APK**
   ```bash
   cd /home/runner/work/volume-mixer/volume-mixer
   ./gradlew assembleDebug
   ```

2. **Install on Device**
   ```bash
   # Via Gradle
   ./gradlew installDebug
   
   # Or via ADB directly
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Launch the App**
   ```bash
   adb shell am start -n com.volumemixer/.MainActivity
   ```

## Manual Test Cases

### Test Case 1: Service Starts Successfully
**Steps:**
1. Open Volume Mixer app
2. Verify you see "Service is stopped" text
3. Tap "Start Service" button
4. Verify button text changes to "Stop Service"
5. Verify status text changes to "Service is running"
6. Pull down notification shade
7. Verify notification shows with title "Volume Mixer Active"

**Expected Result:**
✅ Service starts
✅ Notification appears
✅ UI updates correctly

### Test Case 2: Notification Persistence
**Steps:**
1. Start the service (see Test Case 1)
2. Try to swipe away the notification
3. Verify notification cannot be dismissed

**Expected Result:**
✅ Notification remains visible (ongoing)

### Test Case 3: Notification Action
**Steps:**
1. Start the service
2. Pull down notification shade
3. Tap on the notification

**Expected Result:**
✅ MainActivity opens

### Test Case 4: Service Stops Successfully
**Steps:**
1. Start the service
2. Tap "Stop Service" button
3. Verify button text changes to "Start Service"
4. Verify status text changes to "Service is stopped"
5. Pull down notification shade
6. Verify notification is gone

**Expected Result:**
✅ Service stops
✅ Notification disappears
✅ UI updates correctly

### Test Case 5: Service Survives App Background
**Steps:**
1. Start the service
2. Press Home button (put app in background)
3. Pull down notification shade
4. Verify notification is still there
5. Open recent apps and return to Volume Mixer
6. Verify service is still running

**Expected Result:**
✅ Service continues running in background
✅ Notification remains visible
✅ UI shows correct state when returning

### Test Case 6: Service Survives Screen Rotation
**Steps:**
1. Start the service
2. Rotate device (portrait ↔ landscape)
3. Check UI state
4. Check notification

**Expected Result:**
✅ Service continues running
✅ UI may reset state (known limitation)
✅ Notification remains visible

### Test Case 7: Multiple Start Commands
**Steps:**
1. Start the service
2. Tap "Stop Service"
3. Tap "Start Service" again
4. Repeat 2-3 times

**Expected Result:**
✅ Service starts and stops reliably
✅ No crashes or errors
✅ UI updates correctly each time

## Logcat Verification

### Open Logcat
```bash
# Monitor only AudioSessionService logs
adb logcat -s AudioSessionService:D

# Monitor with timestamps
adb logcat -v time -s AudioSessionService:D

# Clear logs and monitor
adb logcat -c && adb logcat -s AudioSessionService:D
```

### Expected Log Messages

**On First Service Start:**
```
D/AudioSessionService: Service created
D/AudioSessionService: Service running
```

**On Subsequent Starts (service already created):**
```
D/AudioSessionService: Service running
```

**On Service Stop:**
```
D/AudioSessionService: Service stopped
```

### Verify Logs
**Steps:**
1. Clear logs: `adb logcat -c`
2. Start service
3. Check logs: `adb logcat -d -s AudioSessionService:D`
4. Verify "Service running" appears
5. Stop service
6. Check logs again
7. Verify "Service stopped" appears

**Expected Result:**
✅ All expected log messages appear
✅ No error or warning messages

## Advanced Testing

### Test on Multiple Android Versions
Test the service on different Android versions:
- ✅ Android 8.0 (API 26) - Minimum supported
- ✅ Android 9.0 (API 28)
- ✅ Android 10 (API 29)
- ✅ Android 11 (API 30)
- ✅ Android 12 (API 31)
- ✅ Android 13 (API 33) - POST_NOTIFICATIONS permission
- ✅ Android 14 (API 34) - Target SDK

### Test Notification Permission (Android 13+)
**Steps:**
1. Install app on Android 13+ device
2. Open app
3. Start service
4. Check if notification appears
5. If not, go to Settings → Apps → Volume Mixer → Notifications
6. Enable notifications
7. Stop and start service again
8. Verify notification appears

**Note:** Runtime permission request not implemented yet (future enhancement)

### Test Service Recovery
**Steps:**
1. Start service
2. Force stop the app: `adb shell am force-stop com.volumemixer`
3. Wait a few seconds
4. Check notification shade

**Expected Result:**
✅ Service stops (notification disappears)
✅ Service does not auto-restart (expected behavior)

**Note:** START_STICKY means service will restart if killed by system (low memory), but not if explicitly stopped.

## Performance Testing

### Memory Usage
```bash
# Check app memory usage
adb shell dumpsys meminfo com.volumemixer

# Monitor memory continuously
adb shell top | grep volumemixer
```

**Expected Result:**
✅ Memory usage remains stable
✅ No memory leaks over time

### Battery Usage
**Steps:**
1. Start service
2. Let it run for 30 minutes
3. Go to Settings → Battery → Battery usage
4. Check Volume Mixer battery consumption

**Expected Result:**
✅ Battery usage is minimal (notification only)
✅ No significant battery drain

## Debugging Issues

### Service Not Starting
**Check:**
1. Logcat for exceptions
2. Permissions in manifest
3. Service registration in manifest
4. Build and install succeeded

**Commands:**
```bash
# View all logs
adb logcat

# View crash logs only
adb logcat *:E

# Check service status
adb shell dumpsys activity services com.volumemixer
```

### Notification Not Appearing
**Check:**
1. Notification permission (Android 13+)
2. Notification channel created
3. Logcat for notification-related errors
4. App info → Notifications enabled

**Commands:**
```bash
# Check notification settings
adb shell dumpsys notification

# Check if channel exists
adb shell dumpsys notification | grep volume_mixer_channel
```

### App Crashes
**Check:**
```bash
# View crash logs
adb logcat | grep -i "fatal\|exception\|crash"

# Get crash stack trace
adb logcat *:E

# Check ANR logs
adb shell ls /data/anr/
```

## Test Results Template

Use this template to record test results:

```markdown
## Test Session Results

**Date:** [Date]
**Device:** [Device Model]
**Android Version:** [API Level]
**Build:** [Debug/Release]

### Test Case Results

| Test Case | Status | Notes |
|-----------|--------|-------|
| Service Starts | ✅/❌ | |
| Notification Persistence | ✅/❌ | |
| Notification Action | ✅/❌ | |
| Service Stops | ✅/❌ | |
| Survives Background | ✅/❌ | |
| Survives Rotation | ✅/❌ | |
| Multiple Start/Stop | ✅/❌ | |
| Logcat Messages | ✅/❌ | |

### Issues Found
[List any issues or unexpected behavior]

### Overall Result
✅ PASS / ❌ FAIL

### Additional Notes
[Any other observations]
```

## Automated Testing (Future)

For automated UI testing with Espresso:

```kotlin
@Test
fun testServiceStartButton() {
    onView(withText("Start Service"))
        .check(matches(isDisplayed()))
        .perform(click())
    
    onView(withText("Stop Service"))
        .check(matches(isDisplayed()))
}
```

**Note:** Automated tests for services require instrumentation tests which need an emulator/device.

## Common Issues and Solutions

### Issue: Notification doesn't appear on Android 13+
**Solution:** Manually grant POST_NOTIFICATIONS permission in Settings

### Issue: Service stops when app is swiped away
**Solution:** This is normal for unbound services. To keep running, service needs to be started from a persistent component or use a different approach.

### Issue: UI state resets after rotation
**Solution:** Known limitation. Service state not persisted in UI. Check notification to verify service is actually running.

### Issue: Build fails with network error
**Solution:** The project requires internet access to download dependencies. Check network connection and proxy settings.

## Contact and Support

If you encounter issues during testing:
1. Check logcat for error messages
2. Review the ISSUE_2_COMPLETION_SUMMARY.md for known limitations
3. Consult FOREGROUND_SERVICE_IMPLEMENTATION.md for implementation details
4. Check SERVICE_ARCHITECTURE_DIAGRAM.md for architecture understanding

## Next Steps After Testing

Once manual testing is complete:
1. Document any issues found
2. Verify all test cases pass
3. Consider implementing runtime permission request for Android 13+
4. Proceed with audio session tracking implementation (Phase 2)

---

**Last Updated:** October 18, 2025
**Status:** Ready for Testing
