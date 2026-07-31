#include <jni.h>
#include <cstdlib>
#include <cstring>
#include <cerrno>
#include <unistd.h>
#include <vector>
#include <fcntl.h>
#include <pty.h>
#include <sys/ioctl.h>
#include <android/log.h>

#define TAG "ubuterm-pty"
#define LOG(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

struct PtyHandle {
    int master;
    int slave;
};

static jlong handleToLong(PtyHandle* h) { return reinterpret_cast<jlong>(h); }
static PtyHandle* longToHandle(jlong v) { return reinterpret_cast<PtyHandle*>(v); }

extern "C" JNIEXPORT jlong JNICALL
Java_com_ubuterm_terminal_PtyBridge_openPty(JNIEnv*, jobject, jint cols, jint rows) {
    int master = -1, slave = -1;
    if (openpty(&master, &slave, nullptr, nullptr, nullptr) != 0) {
        LOG("openpty failed: %s", strerror(errno));
        return 0;
    }
    winsize ws{};
    ws.ws_col = static_cast<unsigned short>(cols);
    ws.ws_row = static_cast<unsigned short>(rows);
    ioctl(master, TIOCSWINSZ, &ws);
    fcntl(master, F_SETFL, O_NONBLOCK);
    auto* h = new PtyHandle{master, slave};
    LOG("pty opened master=%d slave=%d", master, slave);
    return handleToLong(h);
}

extern "C" JNIEXPORT void JNICALL
Java_com_ubuterm_terminal_PtyBridge_writeToPty(JNIEnv* env, jobject, jlong handle, jbyteArray data) {
    auto* h = longToHandle(handle);
    if (!h) return;
    jsize len = env->GetArrayLength(data);
    jbyte* buf = env->GetByteArrayElements(data, nullptr);
    if (buf) {
        write(h->master, buf, static_cast<size_t>(len));
        env->ReleaseByteArrayElements(data, buf, JNI_ABORT);
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_ubuterm_terminal_PtyBridge_readFromPty(JNIEnv* env, jobject, jlong handle, jbyteArray buffer) {
    auto* h = longToHandle(handle);
    if (!h) return -1;
    jsize cap = env->GetArrayLength(buffer);
    std::vector<char> tmp(static_cast<size_t>(cap));
    ssize_t n = read(h->master, tmp.data(), static_cast<size_t>(cap));
    if (n <= 0) return static_cast<jint>(n);
    env->SetByteArrayRegion(buffer, 0, static_cast<jsize>(n), reinterpret_cast<jbyte*>(tmp.data()));
    return static_cast<jint>(n);
}

extern "C" JNIEXPORT void JNICALL
Java_com_ubuterm_terminal_PtyBridge_setPtySize(JNIEnv*, jobject, jlong handle, jint cols, jint rows) {
    auto* h = longToHandle(handle);
    if (!h) return;
    winsize ws{};
    ws.ws_col = static_cast<unsigned short>(cols);
    ws.ws_row = static_cast<unsigned short>(rows);
    ioctl(h->master, TIOCSWINSZ, &ws);
}

extern "C" JNIEXPORT void JNICALL
Java_com_ubuterm_terminal_PtyBridge_closePty(JNIEnv*, jobject, jlong handle) {
    auto* h = longToHandle(handle);
    if (!h) return;
    close(h->master);
    close(h->slave);
    delete h;
    LOG("pty closed");
}
