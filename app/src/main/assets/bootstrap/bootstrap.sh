#!/bin/bash
# Runtime copy of distro/scripts/install_rootfs.sh for in-app bootstrapping
set -euo pipefail
ROOTFS="$1"
mkdir -p "$ROOTFS/home/user" "$ROOTFS/etc/apt" "$ROOTFS/var/lib/dpkg" "$ROOTFS/var/cache/apt"
echo "deb http://archive.ubuntu.com/ubuntu noble main restricted universe multiverse" > "$ROOTFS/etc/apt/sources.list"
echo "user:x:1000:1000::/home/user:/bin/bash" > "$ROOTFS/etc/passwd"
echo "user:x:1000:" > "$ROOTFS/etc/group"
