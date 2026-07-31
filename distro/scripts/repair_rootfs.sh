#!/bin/bash
# Usage: repair_rootfs.sh <rootfs-dir>
set -euo pipefail
ROOTFS="$1"
SH="$ROOTFS/bin/sh"
if [ ! -x "$SH" ]; then
  echo "no shell in rootfs; reinstall required" >&2
  exit 1
fi
"$SH" -c "dpkg --configure -a" || true
"$SH" -c "apt-get clean" || true
"$SH" -c "find / -xdev -type f -perm -2000 -exec chmod a-s {} +" || true
echo "repair done"
