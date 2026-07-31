package com.ubuterm.terminal

data class TerminalSession(
    val id: String,
    val title: String,
    val distroName: String,
    val createdAt: Long,
    val lastActiveAt: Long,
    val cwd: String,
    val isRunning: Boolean,
    val ptyFd: Int? = null,
    val processId: Int? = null
) {
    companion object {
        fun new(id: String, distroName: String) = TerminalSession(
            id = id,
            title = "session-$id",
            distroName = distroName,
            createdAt = System.currentTimeMillis(),
            lastActiveAt = System.currentTimeMillis(),
            cwd = "~",
            isRunning = false
        )
    }
}
