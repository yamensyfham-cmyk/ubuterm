package com.ubuterm.terminal

object PtyBridge {
    init {
        System.loadLibrary("ubuterm-native")
    }

    external fun openPty(cols: Int, rows: Int): Long
    external fun writeToPty(handle: Long, data: ByteArray)
    external fun readFromPty(handle: Long, buffer: ByteArray): Int
    external fun setPtySize(handle: Long, cols: Int, rows: Int)
    external fun closePty(handle: Long)
    external fun launchProot(rootfsPath: String, masterFd: Int)
    external fun trackChild(pid: Int)
    external fun killChild(signal: Int): Int
}
