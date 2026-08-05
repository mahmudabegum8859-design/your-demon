#!/bin/sh
APK=/tmp/sbin/apk.static
ROOTFS=/tmp/yourdemon-rootfs
PACKAGES="nmap nmap-scripts aircrack-ng hydra john python3 py3-pip py3-wcwidth py3-flask py3-requests py3-colorama wpa_supplicant iw wireless-tools iproute2 openssh curl wget git file which sudo"

echo "=== Installing packages ==="
$APK --root $ROOTFS --arch aarch64 \
  --repository "https://dl-cdn.alpinelinux.org/alpine/latest-stable/main" \
  --repository "https://dl-cdn.alpinelinux.org/alpine/latest-stable/community" \
  --allow-untrusted add $PACKAGES 2>&1

echo "=== Checking ==="
for pkg in nmap hydra john python3; do
  if [ -f "$ROOTFS/usr/bin/$pkg" ] || [ -f "$ROOTFS/usr/sbin/$pkg" ]; then
    echo "FOUND: $pkg"
  else
    echo "MISSING: $pkg"
  fi
done
du -sh $ROOTFS
echo "=== Done ==="
