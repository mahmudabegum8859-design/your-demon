# /CORE/PixieWps — WPS Attack Engine (OneShot-Extended)

The app's WPS screens (`PixieDust`, `BruteWps`, `CustomPin`, `WpsInterfaceFragment`)
invoke this script as:

```
python3 -u /CORE/PixieWps/pixie.py -i <iface> [--iface-down] -B|-p <pin>|-K -F -b <bssid>
```

## Layout inside each core tarball

```
release/CORE/PixieWps/
├── pixie.py      ← compatibility wrapper (translates legacy -K → -P, execs ose.py)
├── ose.py        ← OneShot-Extended main script (https://github.com/chkndrp/OneShot-Extended)
├── src/          ← OneShot-Extended python package (absolute src.* imports only)
└── vulnwsc.txt   ← vulnerable WPS version list (required by ose.py)
```

## How it works

- **No monitor mode needed** — OneShot performs WPS attacks via its own
  wpa_supplicant session (raw WPS exchanges), so it works on the phone's
  built-in WiFi chip with root.
- Required binaries (all already present in the cores): `pixiewps`,
  `wpa_supplicant`, `iw`, `ip`. Python 3.10+ (cores ship 3.14).
- The wrapper translates the original-OneShot flag `-K` (pixie dust) to the
  OneShot-Extended flag `-P`, so the existing app command strings keep working.
- `logger.success()` already prints `[+] WPS PIN:` / `[+] WPA PSK:`, which the
  app's result parsers expect.

## Rebuilding / updating

```sh
# 1) Fresh clone
git clone --depth 1 https://github.com/chkndrp/OneShot-Extended /tmp/oneshot

# 2) Copy into a core rootfs (after extraction):
mkdir -p <rootfs>/CORE/PixieWps
cp /tmp/oneshot/ose.py /tmp/oneshot/vulnwsc.txt <rootfs>/CORE/PixieWps/
cp -r /tmp/oneshot/src <rootfs>/CORE/PixieWps/src
cp chroot/PixieWps/pixie.py <rootfs>/CORE/PixieWps/pixie.py
rm -rf <rootfs>/CORE/PixieWps/src/__pycache__

# 3) Re-tar as release/CORE/...
tar -czf core-<arch>.tar.gz -C <parent> release
```

## Version history

- v2.3.0 — first bundled WPS engine (OneShot-Extended) in all 4 cores.
