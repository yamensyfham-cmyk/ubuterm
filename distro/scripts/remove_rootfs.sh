#!/bin/bash
# Usage: remove_rootfs.sh <rootfs-dir>
set -euo pipefail
ROOTFS="$1"
if [ -d "$ROOTFS" ]; then
  rm -rf "$ROOTFS"
fi
echo "removed"
