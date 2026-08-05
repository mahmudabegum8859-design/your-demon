#!/bin/bash
# Your Demon - Alpine Linux Chroot Builder
# Builds ARM64 (aarch64) and ARM32 (armv7) chroot tarballs
# Uses apk --root for cross-architecture builds (no QEMU needed)
# Usage: ./build-chroot.sh [arch]
#   arch: aarch64 (default) or armv7

set -euo pipefail

ARCH="${1:-aarch64}"
ALPINE_VERSION="latest-stable"
ALPINE_MIRROR="https://dl-cdn.alpinelinux.org/alpine"
ROOTFS_DIR="/tmp/yourdemon-chroot-${ARCH}"
OUTPUT_DIR="$(dirname "$0")/output"
TARBALL="core-${ARCH}.tar.gz"

# Map arch to Alpine arch
case "$ARCH" in
  aarch64|arm64)
    ALPINE_ARCH="aarch64"
    ;;
  armv7|arm32|armhf)
    ALPINE_ARCH="armv7"
    ;;
  *)
    echo "Unsupported architecture: $ARCH"
    echo "Use: aarch64 (arm64) or armv7 (arm32)"
    exit 1
    ;;
esac

APK_TOOL="apk.static"
APK_URL="https://dl-cdn.alpinelinux.org/alpine/${ALPINE_VERSION}/main/x86_64/${APK_TOOL}"
REPO_MAIN="${ALPINE_MIRROR}/${ALPINE_VERSION}/main"
REPO_COMMUNITY="${ALPINE_MIRROR}/${ALPINE_VERSION}/community"
REPO_TESTING="${ALPINE_MIRROR}/edge/testing"

echo "=== Building Your Demon Chroot ==="
echo "Target Architecture: ${ALPINE_ARCH}"
echo "Rootfs: ${ROOTFS_DIR}"
echo ""

# Clean up
rm -rf "$ROOTFS_DIR"
mkdir -p "$ROOTFS_DIR" "$OUTPUT_DIR"

# Download apk.static if not present
if [ ! -f "/tmp/${APK_TOOL}" ]; then
  echo "Downloading ${APK_TOOL}..."
  curl -sL "$APK_URL" -o "/tmp/${APK_TOOL}"
  chmod +x "/tmp/${APK_TOOL}"
fi
APK="/tmp/${APK_TOOL}"

# Initialize Alpine rootfs
echo "Initializing Alpine rootfs for ${ALPINE_ARCH}..."
$APK --root "$ROOTFS_DIR" --arch "$ALPINE_ARCH" \
  --repository "$REPO_MAIN" \
  --repository "$REPO_COMMUNITY" \
  --initdb add alpine-base

# Write resolver config
echo "nameserver 1.1.1.1" > "$ROOTFS_DIR/etc/resolv.conf"

# Install packages
echo "Installing packages..."
$APK --root "$ROOTFS_DIR" --arch "$ALPINE_ARCH" \
  --repository "$REPO_MAIN" \
  --repository "$REPO_COMMUNITY" \
  add --no-cache \
    busybox \
    bash \
    nmap \
    nmap-scripts \
    aircrack-ng \
    hydra \
    john \
    sqlmap \
    python3 \
    py3-pip \
    py3-tkinter \
    py3-wcwidth \
    py3-flask \
    wpasupplicant \
    pixiewps \
    iw \
    wireless-tools \
    iproute2 \
    openssh \
    curl \
    wget \
    git \
    file \
    which \
    sudo

# Install Python packages
echo "Installing Python packages..."
$APK --root "$ROOTFS_DIR" --arch "$ALPINE_ARCH" \
  --repository "$REPO_MAIN" \
  --repository "$REPO_COMMUNITY" \
  add --no-cache py3-requests py3-colorama

# Clone opx-oneshot
echo "Installing opx-oneshot..."
mkdir -p "$ROOTFS_DIR/opt"
git clone --depth=1 https://github.com/OP-AMINUL-FF/opx-oneshot.git "$ROOTFS_DIR/opt/opx-oneshot"
mkdir -p "$ROOTFS_DIR/usr/local/bin"
ln -sf /opt/opx-oneshot/oneshot.py "$ROOTFS_DIR/usr/local/bin/oneshot"

# Create mount points
mkdir -p "$ROOTFS_DIR/data/local/yourdemon"
mkdir -p "$ROOTFS_DIR/storage/emulated/0"
mkdir -p "$ROOTFS_DIR/dev"
mkdir -p "$ROOTFS_DIR/proc"
mkdir -p "$ROOTFS_DIR/sys"

# Create release info
echo "v1.0" > "$ROOTFS_DIR/etc/yourdemon-release"
echo "Your Demon Chroot v1.0 (${ALPINE_ARCH})" >> "$ROOTFS_DIR/etc/yourdemon-release"
echo "Built: $(date -u '+%Y-%m-%d %H:%M UTC')" >> "$ROOTFS_DIR/etc/yourdemon-release"

# Clean up
rm -f "$ROOTFS_DIR/etc/resolv.conf"
rm -rf "$ROOTFS_DIR/var/cache/apk/*"

# Package tarball
echo "Packaging ${TARBALL}..."
cd "$ROOTFS_DIR"
tar -czf "${OUTPUT_DIR}/${TARBALL}" .
echo ""
echo "=== Build Complete ==="
echo "Output: ${OUTPUT_DIR}/${TARBALL}"
ls -lh "${OUTPUT_DIR}/${TARBALL}"
echo ""
echo "To upload: gh release upload v1.0 \"${OUTPUT_DIR}/${TARBALL}\" --repo OP-AMINUL-FF/your-demon-chroot"
