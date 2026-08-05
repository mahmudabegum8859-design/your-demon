## <center> Your Demon</center>

[中文介绍](./docs/zh-CN/README.md)

> **Your Demon** — Android Penetration Testing Suite by OPX
> Forked from [Stryker App](https://github.com/stryker-project/app) v4.0 (EOL)

###### Requirements

1. Android 8.0+ (API 26)
2. Root access (Magisk 23.0+)
3. 64-bit CPU (preferred)
4. 4 GB+ free space (for chroot environment)

###### Features

- **WiFi Security Auditing** — WPS testing, handshake capture, network scanning
- **Chroot Environment** — Full Alpine Linux with nmap, hydra, John the Ripper, aircrack-ng, sqlmap, and more
- **Module System** — Extensible exploit/scan modules (EternalBlue, SMBGhost, etc.)
- **Network Tools** — Port scanning, SMB enumeration, packet capture, ARP scanning
- **Brute Force** — Hydra-based authentication testing
- **Automatic Updates** — Self-updating via GitHub Releases

###### Resources

| | |
|---|---|
| **Source Code** | [github.com/OP-AMINUL-FF/your-demon](https://github.com/OP-AMINUL-FF/your-demon) |
| **Releases** | [github.com/OP-AMINUL-FF/your-demon/releases](https://github.com/OP-AMINUL-FF/your-demon/releases) |
| **Chroot Images** | [github.com/OP-AMINUL-FF/your-demon-chroot](https://github.com/OP-AMINUL-FF/your-demon-chroot) |
| **Modules** | [github.com/OP-AMINUL-FF/your-demon-modules](https://github.com/OP-AMINUL-FF/your-demon-modules) |

###### Installation

1. Download the latest APK from [Releases](https://github.com/OP-AMINUL-FF/your-demon/releases)
2. Install on a rooted device: `adb install app-debug.apk`
3. Open the app — chroot images download automatically on first launch
4. Grant root permissions when prompted

###### Building from Source

```bash
git clone https://github.com/OP-AMINUL-FF/your-demon.git
cd your-demon
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

###### License

GNU General Public License v2.0 — See [License](./License)
