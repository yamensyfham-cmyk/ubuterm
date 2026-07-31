#!/bin/bash
# Usage: update_rootfs.sh <rootfs-dir>
set -euo pipefail
ROOTFS="$1"
"$ROOTFS/bin/sh" -c "apt-get update && apt-get upgrade -y"
echo "update done"
