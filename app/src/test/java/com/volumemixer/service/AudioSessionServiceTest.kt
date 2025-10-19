package com.volumemixer.service

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AudioSessionService.
 * Note: Full service testing requires instrumentation tests with an Android device/emulator.
 * These are basic structural tests.
 */
class AudioSessionServiceTest {

    @Test
    fun serviceActions_areDefinedCorrectly() {
        assertEquals("com.volumemixer.action.START", AudioSessionService.ACTION_START)
        assertEquals("com.volumemixer.action.STOP", AudioSessionService.ACTION_STOP)
        assertEquals("com.volumemixer.action.SESSION_CHANGED", AudioSessionService.ACTION_SESSION_CHANGED)
    }

    @Test
    fun serviceConstants_areNotNull() {
        assertNotNull(AudioSessionService.ACTION_START)
        assertNotNull(AudioSessionService.ACTION_STOP)
        assertNotNull(AudioSessionService.ACTION_SESSION_CHANGED)
        assertNotNull(AudioSessionService.EXTRA_SESSION_DATA)
    }

    @Test
    fun sessionChangedAction_hasCorrectValue() {
        assertEquals("com.volumemixer.action.SESSION_CHANGED", AudioSessionService.ACTION_SESSION_CHANGED)
    }

    @Test
    fun sessionDataExtra_hasCorrectValue() {
        assertEquals("com.volumemixer.extra.SESSION_DATA", AudioSessionService.EXTRA_SESSION_DATA)
    }

    @Test
    fun broadcastIntentFormat_parsesCorrectly() {
        // Test the format of session data broadcast
        val sessionData = arrayOf(
            "com.spotify:true",
            "com.youtube:false"
        )

        val spotify = sessionData[0].split(":")
        val youtube = sessionData[1].split(":")

        assertEquals("com.spotify", spotify[0])
        assertEquals("true", spotify[1])
        assertTrue(spotify[1].toBoolean())

        assertEquals("com.youtube", youtube[0])
        assertEquals("false", youtube[1])
        assertFalse(youtube[1].toBoolean())
    }
}
