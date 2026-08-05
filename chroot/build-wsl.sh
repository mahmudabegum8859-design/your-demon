#!/bin/sh
# Your Demon - Alpine Linux Chroot Builder (WSL root version)
# Builds aarch64 or armv7 chroot tarball
# Usage: sh build-wsl.sh [aarch64|armv7]

ARCH="${1:-aarch64}"
case "$ARCH" in
  aarch64|arm64) ALPINE_ARCH="aarch64"; OUT="core64.tar.gz" ;;
  armv7|arm32|armhf) ALPINE_ARCH="armv7"; OUT="core32.tar.gz" ;;
  *) echo "Usage: $0 [aarch64|armv7]"; exit 1 ;;
esac

APK=/tmp/sbin/apk.static
ROOTFS=/tmp/yourdemon-rootfs-$ALPINE_ARCH
OUTPUT_DIR="/mnt/c/Users/uer/New folder (2)/strykerapp-4.0/strykerapp-4.0/chroot/output"

rm -rf $ROOTFS
mkdir -p $ROOTFS $OUTPUT_DIR

echo "=== Initializing Alpine $ALPINE_ARCH rootfs ==="
$APK --root $ROOTFS --arch $ALPINE_ARCH \
  --repository "https://dl-cdn.alpinelinux.org/alpine/latest-stable/main" \
  --repository "https://dl-cdn.alpinelinux.org/alpine/latest-stable/community" \
  --allow-untrusted --initdb add alpine-base 2>&1 | grep -v "execve: Exec format error"
echo "Init exit code: $?"

echo "nameserver 1.1.1.1" > $ROOTFS/etc/resolv.conf

echo "=== Installing packages ==="
$APK --root $ROOTFS --arch $ALPINE_ARCH \
  --repository "https://dl-cdn.alpinelinux.org/alpine/latest-stable/main" \
  --repository "https://dl-cdn.alpinelinux.org/alpine/latest-stable/community" \
  --repository "https://dl-cdn.alpinelinux.org/alpine/edge/testing" \
  --allow-untrusted add \
    busybox bash \
    nmap nmap-scripts \
    aircrack-ng \
    hydra \
    john \
    python3 py3-pip py3-wcwidth py3-flask py3-tk \
    py3-requests py3-colorama \
    wpa_supplicant pixiewps \
    iw wireless-tools \
    iproute2 openssh curl wget git file which sudo 2>&1 | grep -v "execve: Exec format error"
echo "Packages exit code: $?"

echo "=== Installing sqlmap via pip ==="
# sqlmap is not in Alpine repos, install via pip
mkdir -p $ROOTFS/opt
cat > $ROOTFS/opt/install-pips.sh << 'PIPEOF'
#!/bin/sh
# Run this on first boot to install pip packages
pip3 install --break-system-packages sqlmap
PIPEOF
chmod +x $ROOTFS/opt/install-pips.sh

echo "=== Installing opx-oneshot ==="
git clone --depth=1 https://github.com/OP-AMINUL-FF/opx-oneshot.git $ROOTFS/opt/opx-oneshot 2>/dev/null || echo "Git clone skipped"
mkdir -p $ROOTFS/usr/local/bin
ln -sf /opt/opx-oneshot/oneshot.py $ROOTFS/usr/local/bin/oneshot 2>/dev/null || true

echo "=== Creating directories ==="
for d in data/local/yourdemon storage/emulated/0 dev proc sys opt; do
  mkdir -p $ROOTFS/$d
done

echo "=== Release info ==="
echo "v1.0" > $ROOTFS/etc/yourdemon-release
echo "Your Demon Chroot v1.0 ($ALPINE_ARCH)" >> $ROOTFS/etc/yourdemon-release
echo "Built: $(date -u '+%Y-%m-%d %H:%M UTC')" >> $ROOTFS/etc/yourdemon-release

echo "=== Cleaning up ==="
rm -f $ROOTFS/etc/resolv.conf
rm -rf $ROOTFS/var/cache/apk/*
rm -rf $ROOTFS/root/.cache 2>/dev/null || true

echo "=== Packaging $OUT ==="
cd $ROOTFS && tar -czf $OUTPUT_DIR/$OUT .
ls -lh $OUTPUT_DIR/$OUT
echo "=== DONE ==="
