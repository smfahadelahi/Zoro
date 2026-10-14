# Prototype release checklist

- [ ] Browser regression suite passes (`node tests/prototype.cjs`).
- [ ] `./gradlew :prototype:assembleDebug` succeeds.
- [ ] APK contains bundled `assets/index.html`; no permissions in merged manifest.
- [ ] Test install on Android 7.0+ with a current Android System WebView.
- [ ] Onboarding, keyboard send, bottom sheets and hardware back work on a phone.
- [ ] Airplane mode works; notes/settings survive relaunch.
- [ ] Timers/alarms clearly described as in-app only.
- [ ] UI PIN explicitly described as a simulation, not secure authentication.
- [ ] Release includes APK, standalone HTML, SHA256 sums and limitations.
- [ ] Production distribution uses a real signing key and security review instead of this test build.
