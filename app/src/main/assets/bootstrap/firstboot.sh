#!/bin/bash
export HOME=/home/user
export USER=user
apt-get update -qq
apt-get install -y --no-install-recommends bash coreutils procps ca-certificates curl nano locales sudo
echo "en_US.UTF-8 UTF-8" > /etc/locale.gen
locale-gen
ln -sf /usr/share/zoneinfo/UTC /etc/localtime
apt-get clean
