## Your Demon v2.3.0 — WPS Fixed with OneShot-Extended

### What's new in v2.3.0
- **WPS attacks now work!** Pixie Dust, PIN brute force, custom PIN and the WPS Interface screen all run **OneShot-Extended** (https://github.com/chkndrp/OneShot-Extended) bundled inside every core — **no monitor mode needed**, works with the phone's built-in WiFi chip under root.
- **All 4 cores rebuilt** (arm64, armv7, x86_64, x86) with the WPS engine at `/CORE/PixieWps/`:
  - `pixie.py` — compatibility wrapper (translates the legacy `-K` pixie-dust flag to OneShot-Extended's `-P`, so the existing app screens work unchanged)
  - `ose.py` + `src/` + `vulnwsc.txt` — OneShot-Extended full source
- **Fixed a command bug** in Custom PIN mode (missing space before `-b` made the BSSID merge into the PIN argument).
- **WPS Interface screen** switched from `reaver` (not available in Alpine) to the bundled OneShot engine, with automatic WiFi disable/enable around the attack.
- Success output (`[+] WPS PIN:` / `[+] WPA PSK:`) matches the app's result parser, so cracked credentials show up in the UI.
- All previous fixes carried over: sqlmap (Python 3.14 crypt fix), arp-scan, macchanger.

### Requirements
- Rooted device (the app already handles this)
- Tested targets: WPS-enabled access points
- Best results on WPS 1.0 (vulnerable) routers via the Pixie Dust attack

### Install
1. Install the APK (release build recommended).
2. Update to get the new core, or reinstall for a fresh setup — the core now includes the WPS engine.
3. Open WiFi → WPS attacks → Pixie Dust / Brute Force / Custom PIN.

### Files
- APK: `app-release.apk` (signed) + `app-debug.apk`
- Cores: `core-aarch64.tar.gz`, `core-armv7.tar.gz`, `core-x86_64.tar.gz`, `core-x86.tar.gz`
