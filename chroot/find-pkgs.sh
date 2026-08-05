#!/bin/sh
APK=/tmp/sbin/apk.static
TMP=/tmp/pkg-check
rm -rf $TMP
mkdir -p $TMP
$APK --root $TMP --arch x86_64 \
  --repository "https://dl-cdn.alpinelinux.org/alpine/latest-stable/main" \
  --repository "https://dl-cdn.alpinelinux.org/alpine/latest-stable/community" \
  --repository "https://dl-cdn.alpinelinux.org/alpine/edge/testing" \
  --allow-untrusted --initdb 2>/dev/null

echo "=== Searching packages ==="
for pkg in pixiewps wpa_supplicant wpasupplicant sqlmap tkinter python3-tk pixie; do
  echo "--- Searching: $pkg ---"
  $APK --root $TMP --allow-untrusted search "$pkg" 2>/dev/null || echo "Not found"
done

rm -rf $TMP
echo "=== Done ==="
