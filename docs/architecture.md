# Ubuterm architecture

## Layers
1. UI (Kotlin, single Activity)
2. Terminal session model (TerminalSession)
3. PTY engine (C++ NDK, pty_bridge.cpp)
4. Distro manager (DistroManager.kt)
5. Storage: app-specific external dir (rootfs/, archives/, logs/)
6. Security: SAF for user files, checksum verify, repair mode

## Flow
- install: archive -> verify sha256 -> tar extract -> bootstrap -> first boot apt
- run: openPty() -> launchProot(rootfs) -> stream read/write
- repair: dpkg --configure -a -> apt clean

## Anti-patterns (do not)
- No chroot without root
- No WebView terminal
- No shared-storage writes outside SAF
- No background exec assumptions (Android 12+/14+)
