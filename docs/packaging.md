# Packaging

1. Build rootfs: debootstrap jammy to dir, tar -czf ubuntu-base.tar.gz
2. Write sha256: `sha256sum ubuntu-base.tar.gz > ubuntu-base.tar.gz.sha256`
3. Drop both in app archives dir or fetch URL
4. gradle assembleRelease; R8 minify on
