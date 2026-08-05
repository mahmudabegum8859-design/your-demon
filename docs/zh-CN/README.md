## <center> Your Demon</center>

[English](../../README.md)

###### 运行要求

1. 安卓 8.0+ (API 26)
2. 已获取Root权限 (Magisk 23.0+)
3. 64位处理器 (推荐)
4. 4 GB+ 可用空间 (用于chroot环境)

###### 简介

Your Demon 是新一代的移动渗透测试应用程序，由 OPX 开发。它帮助安全爱好者在没有特殊技能和知识的情况下测试网络设备的常见漏洞。支持添加自定义利用脚本和模块！

###### 功能

- **WiFi 安全审计** — WPS 测试、握手包捕获、网络扫描
- **Chroot 环境** — 完整 Alpine Linux，预装 nmap、hydra、John the Ripper、aircrack-ng、sqlmap 等工具
- **模块系统** — 可扩展的利用/扫描模块（EternalBlue、SMBGhost 等）
- **网络工具** — 端口扫描、SMB 枚举、数据包捕获、ARP 扫描
- **暴力破解** — 基于 Hydra 的认证测试
- **自动更新** — 通过 GitHub Releases 自动更新

###### 链接

| | |
|---|---|
| **源代码** | [github.com/OP-AMINUL-FF/your-demon](https://github.com/OP-AMINUL-FF/your-demon) |
| **发布页** | [github.com/OP-AMINUL-FF/your-demon/releases](https://github.com/OP-AMINUL-FF/your-demon/releases) |
| **Chroot 镜像** | [github.com/OP-AMINUL-FF/your-demon-chroot](https://github.com/OP-AMINUL-FF/your-demon-chroot) |
| **模块仓库** | [github.com/OP-AMINUL-FF/your-demon-modules](https://github.com/OP-AMINUL-FF/your-demon-modules) |

###### 安装

1. 从 [发布页](https://github.com/OP-AMINUL-FF/your-demon/releases) 下载最新 APK
2. 在已Root的设备上安装：`adb install app-debug.apk`
3. 打开应用 — chroot 镜像会在首次启动时自动下载
4. 弹出Root权限请求时点击允许

###### 许可证

GNU General Public License v2.0
