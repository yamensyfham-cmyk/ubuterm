#include <jni.h>
#include <cstring>
#include <cstdlib>
#include <cerrno>
#include <unistd.h>
#include <vector>
#include <string>
#include <android/log.h>

#define TAG "ubuterm-proot"
#define LOG(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

extern "C" JNIEXPORT void JNICALL
Java_com_ubuterm_terminal_PtyBridge_launchProot(JNIEnv* env, jobject, jstring rootfsPath, jint masterFd) {
    const char* rootfs = env->GetStringUTFChars(rootfsPath, nullptr);
    LOG("launch proot at %s", rootfs);

    dup2(masterFd, 0);
    dup2(masterFd, 1);
    dup2(masterFd, 2);

    std::vector<std::string> argv = {
        "proot", "-0",
        "-b", std::string(rootfs) + "/proc:/proc",
        "-b", std::string(rootfs) + "/dev:/dev",
        "-r", rootfs,
        "/bin/sh", "-l"
    };
    std::vector<char*> cargv;
    for (auto& a : argv) cargv.push_back(const_cast<char*>(a.c_str()));
    cargv.push_back(nullptr);

    execvp("proot", cargv.data());
    LOG("exec proot failed: %s", strerror(errno));
    env->ReleaseStringUTFChars(rootfsPath, rootfs);
}
