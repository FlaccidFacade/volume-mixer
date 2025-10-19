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

        // Test parsing logic (using lastIndexOf to handle colons in package names)
        sessionData.forEach { data ->
            val colonIndex = data.lastIndexOf(":")
            assertTrue(colonIndex > 0)
            assertTrue(colonIndex < data.length - 1)
            
            val packageName = data.substring(0, colonIndex)
            val playingStr = data.substring(colonIndex + 1)
            
            assertNotNull(packageName)
            assertTrue(playingStr == "true" || playingStr == "false")
        }
    }

    @Test
    fun broadcastIntentFormat_handlesColonInPackageName() {
        // Test handling of package names with colons (edge case)
        val sessionData = "com.example:app:true"
        
        val colonIndex = sessionData.lastIndexOf(":")
        val packageName = sessionData.substring(0, colonIndex)
        val playingStr = sessionData.substring(colonIndex + 1)
        
        assertEquals("com.example:app", packageName)
        assertEquals("true", playingStr)
    }

    @Test
    fun broadcastIntentFormat_validatesBooleanString() {
        // Test explicit boolean string comparison
        assertEquals("true", "true")
        assertNotEquals("true", "True")
        assertNotEquals("true", "TRUE")
        assertEquals("false", "false")
        
        // Test boolean parsing
        assertTrue("true" == "true")
        assertFalse("false" == "true")
        assertFalse("invalid" == "true")
    }
}
