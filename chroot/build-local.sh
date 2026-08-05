#!/bin/bash
# Your Demon - Alpine Linux Chroot Builder (local variant)
# Same as build-chroot.sh but without rm operations; uses a unique temp dir.
# Usage: ./build-local.sh [arch]
#   arch: aarch64 (default) or armv7

set -euo pipefail

ARCH="${1:-aarch64}"
ALPINE_VERSION="latest-stable"
ALPINE_MIRROR="https://dl-cdn.alpinelinux.org/alpine"
STAMP="$(date -u +%Y%m%d%H%M%S)"
ROOTFS_DIR="/tmp/yourdemon-chroot-${ARCH}-${STAMP}"
OUTPUT_DIR="$(cd "$(dirname "$0")" && pwd)/output"
TARBALL="core-${ARCH}.tar.gz"

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
APK="/tmp/sbin/${APK_TOOL}"
REPO_MAIN="${ALPINE_MIRROR}/${ALPINE_VERSION}/main"
REPO_COMMUNITY="${ALPINE_MIRROR}/${ALPINE_VERSION}/community"
REPO_TESTING="${ALPINE_MIRROR}/edge/testing"

echo "=== Building Your Demon Chroot ==="
echo "Target Architecture: ${ALPINE_ARCH}"
echo "Rootfs: ${ROOTFS_DIR}"
echo ""

mkdir -p "$ROOTFS_DIR" "$OUTPUT_DIR"

# Download apk.static if not present or if it is a stale/corrupt file (must be an ELF binary)
if [ ! -f "$APK" ] || ! head -c 4 "$APK" 2>/dev/null | od -An -tx1 | grep -q "7f 45 4c 46"; then
  echo "Locating apk-tools-static package..."
  APK_PKG_URL="$(curl -sL --max-time 30 "${ALPINE_MIRROR}/${ALPINE_VERSION}/main/x86_64/" \
    | grep -o 'apk-tools-static-[^"]*\.apk' | sort -u | tail -1)"
  echo "Downloading ${APK_PKG_URL}..."
  curl -sL --max-time 120 -o /tmp/apk-tools-static.apk \
    "${ALPINE_MIRROR}/${ALPINE_VERSION}/main/x86_64/${APK_PKG_URL}"
  tar -xzf /tmp/apk-tools-static.apk -C /tmp sbin/apk.static
  chmod +x "$APK"
fi

# Initialize Alpine rootfs
echo "Initializing Alpine rootfs for ${ALPINE_ARCH}..."
$APK --root "$ROOTFS_DIR" --arch "$ALPINE_ARCH" \
  --repository "$REPO_MAIN" \
  --repository "$REPO_COMMUNITY" \
  --initdb --allow-untrusted --no-scripts add alpine-base

# Write resolver config
echo "nameserver 1.1.1.1" > "$ROOTFS_DIR/etc/resolv.conf"

# Install packages
echo "Installing packages..."
$APK --root "$ROOTFS_DIR" --arch "$ALPINE_ARCH" \
  --repository "$REPO_MAIN" \
  --repository "$REPO_COMMUNITY" \
  --repository "$REPO_TESTING" \
  add --allow-untrusted --no-cache --no-scripts \
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
    python3-tkinter \
    py3-wcwidth \
    py3-flask \
    wpa_supplicant \
    hostapd \
    dnsmasq \
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
  add --allow-untrusted --no-cache --no-scripts py3-requests py3-colorama

# dnsmasq's post-install script normally creates its user/group, but the
# --no-scripts cross-arch install skips it. Without this user dnsmasq refuses
# to start with "unknown user or group: dnsmasq", breaking the evil twin AP.
echo "Creating dnsmasq user/group..."
echo 'dnsmasq:x:97:97:dnsmasq:/var/lib/dnsmasq:/bin/false' >> "$ROOTFS_DIR/etc/passwd"
echo 'dnsmasq:x:97:' >> "$ROOTFS_DIR/etc/group"
mkdir -p "$ROOTFS_DIR/var/lib/dnsmasq"

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

# Create busybox applet symlinks.
# Alpine's busybox post-install normally runs 'busybox --install -s'. Because the
# build uses --no-scripts (cross-arch install), those symlinks are missing and the
# bootroot script's '/bin/uname' chroot check would fail ("Chroot is corrupted").
# So we create the symlinks manually. Skip paths that already have a real file.
echo "Creating busybox applet links..."
BUSYBOX_APPLETS="
    [ [[ acpid add-shell addgroup adduser adjtimex arch arp arping ash awk \
    base64 basename bbconfig bc beep blkdiscard blkid blockdev brctl bunzip2 bzcat bzip2 \
    cal cat chattr chgrp chmod chown chpasswd chroot chvt cksum clear cmp \
    comm cp cpio crond crontab cryptpw cut date dc dd deallocvt delgroup \
    deluser depmod df diff dirname dmesg dnsdomainname dos2unix du dumpkmap echo egrep \
    eject env ether-wake expand expr factor fallocate false fatattr fbset fbsplash fdflush \
    fdisk fgrep find findfs flock fold free fsck fstrim fsync fuser getopt \
    getty grep groups gunzip gzip halt hd head hexdump hostid hostname hwclock \
    id ifconfig ifdown ifenslave ifup init inotifyd insmod install ionice iostat ip \
    ipaddr ipcalc ipcrm ipcs iplink ipneigh iproute iprule iptunnel kbd_mode kill killall \
    killall5 klogd last less link linux32 linux64 ln loadfont loadkmap logger login \
    logread losetup ls lsattr lsmod lsof lsusb lzcat lzma lzop lzopcat makemime \
    md5sum mdev mesg microcom mkdir mkdosfs mkfifo mkfs.vfat mknod mkpasswd mkswap mktemp \
    modinfo modprobe more mount mountpoint mpstat mv nameif nanddump nandwrite nbd-client nc \
    netstat nice nl nmeter nohup nologin nproc nsenter nslookup ntpd od openvt \
    partprobe passwd paste pgrep pidof ping ping6 pipe_progress pivot_root pkill pmap poweroff \
    printenv printf ps pscan pstree pwd pwdx raidautorun rdate rdev readahead readlink \
    realpath reboot reformime remove-shell renice reset resize rev rfkill rm rmdir rmmod \
    route run-parts sed sendmail seq setconsole setfont setkeycodes setlogcons setpriv setserial setsid \
    sh sha1sum sha256sum sha3sum sha512sum showkey shred shuf slattach sleep sort split \
    stat strings stty su sum swapoff swapon switch_root sync sysctl syslogd tac \
    tail tar tee test time timeout top touch tr traceroute traceroute6 tree \
    true truncate tty ttysize tunctl udhcpc udhcpc6 umount uname unexpand uniq unix2dos \
    unlink unlzma unlzop unshare unxz unzip uptime usleep uudecode uuencode vconfig vi \
    vlock volname watch watchdog wc wget which who whoami whois xargs xxd \
    xzcat yes zcat zcip \
"
for applet in $BUSYBOX_APPLETS; do
    [ -z "$applet" ] && continue
    for d in bin usr/bin sbin usr/sbin; do
        if [ ! -e "$ROOTFS_DIR/$d/$applet" ]; then
            ln -s /bin/busybox "$ROOTFS_DIR/$d/$applet" 2>/dev/null || true
        fi
    done
done

# Create release info
echo "v1.0" > "$ROOTFS_DIR/etc/yourdemon-release"
echo "Your Demon Chroot v1.0 (${ALPINE_ARCH})" >> "$ROOTFS_DIR/etc/yourdemon-release"
echo "Built: $(date -u '+%Y-%m-%d %H:%M UTC')" >> "$ROOTFS_DIR/etc/yourdemon-release"

# Package tarball (wrapped in release/ so the app extracts to /data/local/YourDemon/release/...)
echo "Packaging ${TARBALL}..."
WRAP_DIR="/tmp/yourdemon-wrap-${ARCH}-${STAMP}"
mkdir -p "$WRAP_DIR"
cp -a "$ROOTFS_DIR" "$WRAP_DIR/release"
cd "$WRAP_DIR"
tar -czf "${OUTPUT_DIR}/${TARBALL}" release
echo ""
echo "=== Build Complete ==="
echo "Output: ${OUTPUT_DIR}/${TARBALL}"
ls -lh "${OUTPUT_DIR}/${TARBALL}"
echo ""
