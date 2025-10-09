#include <jni.h>
#include <string>

extern "C" JNIEXPORT jboolean JNICALL
Java_com_volumemixer_jni_NativeLib_isRooted(JNIEnv* env, jobject /* this */) {
    // Stub implementation for Magisk root detection
    // TODO: Implement actual root detection logic
    return JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_volumemixer_jni_NativeLib_getVersion(JNIEnv* env, jobject /* this */) {
    std::string version = "1.0.0";
    return env->NewStringUTF(version.c_str());
}
