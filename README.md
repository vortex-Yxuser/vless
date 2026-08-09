# SZXVPN

Android VpnService client shell with an automated GitHub Actions build pipeline.

IMPORTANT:
- The workflow builds the real sing-box Android library (`libbox.aar`) from the pinned
  upstream source and packages it into the APK.
- The Android app accepts VLESS/VMess share links and converts them to sing-box JSON.
- A TUN implementation and the libbox bridge are included.
- Proxy/payload fields are exposed, but an HTTP CONNECT payload is not blindly injected
  into every VLESS/VMess transport; the app only uses it for the explicit HTTP-proxy
  dial path.
- Do not commit private credentials or share links containing secrets.

Build:
1. Push this repository to GitHub.
2. Open Actions -> Build APK.
3. Run workflow.
4. Download the `szxvpn-debug` artifact.

The workflow uses the official sing-box repository and Go/Android NDK toolchain.
