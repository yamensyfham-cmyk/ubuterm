#include <jni.h>
#include <csignal>
#include <unistd.h>
#include <sys/wait.h>
#include <android/log.h>

#define TAG "ubuterm-signal"
#define LOG(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

static pid_t s_child = -1;

extern "C" JNIEXPORT void JNICALL
Java_com_ubuterm_terminal_PtyBridge_trackChild(JNIEnv*, jobject, jint pid) {
    s_child = static_cast<pid_t>(pid);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_ubuterm_terminal_PtyBridge_killChild(JNIEnv*, jobject, jint signal) {
    if (s_child > 0) {
        kill(-s_child, static_cast<int>(signal));
    }
    return s_child;
}
