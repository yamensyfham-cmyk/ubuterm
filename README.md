# Ubuterm

Android terminal app with embedded lightweight Ubuntu (rootless, proot-based).

## Layout
- app/  - Android app (Kotlin + NDK PTY bridge)
- distro/ - Ubuntu profiles, package lists, lifecycle scripts
- docs/ - architecture, security, packaging, release

## Build
Android Studio (AGP 8.2.2, NDK, SDK 34) or:
./gradlew assembleDebug

## Distro lifecycle
install -> verify checksum -> extract -> bootstrap -> first boot apt
repair -> dpkg --configure -a
remove -> keep optional backup archive
