#!/bin/bash
# Usage: install_rootfs.sh <rootfs-dir> <archive>
set -euo pipefail
ROOTFS="$1"
ARCHIVE="$2"
echo "[1/4] extracting archive"
mkdir -p "$ROOTFS"
tar -xzf "$ARCHIVE" -C "$ROOTFS"
echo "[2/4] creating base dirs"
mkdir -p "$ROOTFS/home/user" "$ROOTFS/etc/apt" "$ROOTFS/var/lib/dpkg" "$ROOTFS/var/cache/apt"
echo "deb http://archive.ubuntu.com/ubuntu noble main restricted universe multiverse" > "$ROOTFS/etc/apt/sources.list"
echo "[3/4] bootstrap user"
echo "user:x:1000:1000::/home/user:/bin/bash" > "$ROOTFS/etc/passwd"
echo "user:x:1000:" > "$ROOTFS/etc/group"
echo "[4/4] done"
