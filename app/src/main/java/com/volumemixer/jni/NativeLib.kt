package com.volumemixer.jni

object NativeLib {
    init {
        System.loadLibrary("volumemixer")
    }

    /**
     * Checks if the device is rooted (Magisk detection stub)
     * @return true if root is detected, false otherwise
     */
    external fun isRooted(): Boolean

    /**
     * Gets the native library version
     * @return version string
     */
    external fun getVersion(): String
}
