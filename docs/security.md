# Security model

- rootfs in app-specific external storage; never /system, never shared dirs
- proot -0 rootless emulation; no real root, no chroot
- archive extraction: sha256 verified, excludes dev/proc/sys
- user file access only via SAF picker
- danger patterns warned in-terminal: rm -rf /, dd, mkfs, fork bombs
- keys/secrets -> Android Keystore; logs in app logs/ dir
