# ZORO

**A private, offline AI-agent prototype. Your intelligence. Your device.**

A self-contained HTML app and a Android wrapper with internet permission for model discovery. Dark Material-inspired UI, mobile-first at **360 × 800**, with a centered phone frame on desktop.

> **This is a simulation, not an LLM runtime.** Replies stream from a keyword-matched local answer library. Model downloads, file attachments, and microphone input are simulated. Community discovery uses the live Hugging Face API. There are no model weights, external libraries, fonts, analytics, or network requests outside Hugging Face metadata discovery in the HTML app.

## Download the Android APK

Open **[GitHub Releases](https://github.com/smfahadelahi/Zoro/releases)** and choose the newest **ZORO 1.0 · Prototype** release.

- **`ZORO-v1.0.0-prototype.apk`** — installable Android 7.0+ prototype.
- **`ZORO.html`** — the same app as a single file; open in a modern browser.
- **`SHA256SUMS.txt`** — checksums for both downloads.

On Android, open the APK and, if asked, allow installation from the app you used to download it. This is a **debug-signed prerelease for testing**, not a Play Store or production release. Only INTERNET permission is requested, for public Hugging Face model metadata. No microphone, storage, notification, or alarm permission is requested.

The workflow caches the debug signing key across builds. If the cache expires, Android may require uninstalling a previous prototype before installing a newer one; uninstalling erases its local data. A production distribution should use a dedicated, securely managed release keystore.

## Run without building

Download or open **[`index.html`](index.html)**. Everything is inside this file, including icons, styles, mock data, and JavaScript. No server or package install is required.

For development, optionally serve the repository:

```sh
python3 -m http.server 8080 --bind 0.0.0.0
```

Visit `http://localhost:8080`. A hosted origin and a `file://` URL have separate local storage. Some browsers restrict storage for local files; ZORO falls back to in-memory state if storage is unavailable.

## What works

| Area             | Behavior                                                                                                          |
| ---------------- | ----------------------------------------------------------------------------------------------------------------- |
| Onboarding       | Three slides, dots, skip, model choice; shown once per browser storage                                            |
| Chat             | Keyword-matched replies, token streaming, typing indicator, stop, copy, regenerate, auto-grow composer            |
| Agent actions    | Confirm/cancel cards; auto-execution when “Ask before acting” is off; each tool can be disabled                   |
| Timers           | Real elapsed-time countdown in the open app; completion toast                                                     |
| Alarms           | Next-occurrence in-app alarms, active list, completion toast                                                      |
| Notes            | Saved locally, listed and deleted; manual creation from Notes                                                     |
| Models           | Five defaults, RAM-fit badges for a simulated 6 GB phone, downloads and active selection                          |
| Downloads        | Live size-dependent progress and speed, pause/resume/cancel, completed-model deletion; continue across navigation |
| Community models | Live Hugging Face search (20 results), repository GGUF filenames/sizes, loading/empty/error states                |
| History          | Session chats grouped Today / Yesterday / Earlier, search, open, delete, new chat                                 |
| Personalization  | Editable saved prompt, three response tones, saved memory facts                                                   |
| Language         | English / বাংলা dictionary for main navigation, forms, actions, settings and empty states                         |
| App lock         | Four-digit PIN setup, validation, explicit lock and lock on reload                                                |
| Privacy          | Delete-all confirmation, only Hugging Face metadata requests, defensive localStorage access, escaped user text                    |

### Try these requests

- `Set a timer for 10 minutes`
- `Timer for 5 seconds`
- `Set an alarm for 7:00 AM`
- `Save a note: Pick up a book on Friday`
- `Explain quantum computing simply`
- `How does privacy work?`
- `What do you remember?`
- `১০ মিনিটের টাইমার দিন`
- `নোট: শুক্রবার বই কিনতে হবে`

### Honest boundaries

- **No inference or real model download.** Qwen 2.5 1.5B starts “preloaded” purely for this demo. Model selection updates app state, not a real runtime. The system prompt is saved but does not dynamically alter the canned answer library; tone and memory do affect supported replies.
- **Alarms and timers are not Android services.** They run only while the app is open. Timers use absolute deadlines, so they catch up after browser throttling, but there are no background notifications, system alarms, audio alerts, or wake locks. Closing/reloading loses these session-only actions.
- **Wi-Fi only is a saved simulated policy.** The simulated download connection is always Wi-Fi; model weights are not fetched. This toggle does not restrict the real metadata search.
- **Attachments are sample filenames**, not a real file upload or file reader. The mic only shows a simulated listening toast.
- **PIN is a UI prototype**, stored locally in plain text. It is not encryption, secure authentication, or protection against device/browser inspection. App lock takes effect on explicit lock and reload, not automatically whenever the app is backgrounded.
- **Durable state:** settings (including PIN and onboarding completion), notes, memory facts, downloaded flags, and the active model. **Session-only state:** chats, pending downloads, timers, alarms, draft text, and attachments. Deleting all data clears these and removes every downloaded flag; the first-run walkthrough stays dismissed.
- The Android wrapper uses an intercepted local `https://zoro.local/index.html` origin solely to serve bundled bytes. No DNS lookup or remote request is needed. Only GET requests to `https://huggingface.co/api/models` and its repository metadata paths are allowed; unrelated resources and navigation are blocked. Searches send your query to Hugging Face; chats, notes, memory and settings are never included. Browser clipboard availability varies; the app falls back to legacy copy and reports failures.

## Repository structure

```text
index.html                    Entire web app; single source of truth
prototype/                    Android SDK-only Java WebView shell
  src/main/java/...           Local resource interception and back navigation
  src/main/AndroidManifest.xml  INTERNET only; backups disabled
app/                          Original experimental native app, preserved but not built
.github/workflows/release.yml  Browser tests → APK build → GitHub prerelease
tests/prototype.cjs            Browser end-to-end regression suite
gradlew / gradlew.bat          Gradle 8.9 wrapper
```

The original `app/` sources and version catalog remain unchanged as reference. They are not part of the new Gradle settings or distributed APK. Its original signing configuration is untouched. The new `prototype` module has its own application ID, `app.zoro.prototype`, and does not require Firebase, AI Studio, cloud keys, or the old app's signing keys.

## Build the APK locally

Requirements: JDK 17, Android SDK platform 35 and Build Tools 35.0.0, with `ANDROID_HOME` set or `sdk.dir` in a local (ignored) `local.properties`.

```sh
# macOS/Linux
./gradlew :prototype:assembleDebug

# Windows
gradlew.bat :prototype:assembleDebug
```

Output: `prototype/build/outputs/apk/debug/prototype-debug.apk`.

Gradle copies the root `index.html` into generated Android assets before building. Do not maintain a separate copy inside the module. First-time builds need internet for build tooling; the **installed app only needs internet for Hugging Face discovery**. Chat, notes and other simulations work offline.

## Automated releases

The **Build and release ZORO** GitHub Actions workflow runs on relevant changes pushed to `main` or `arena/91f9439d-zoro`, and can also be manually dispatched when available on the default branch. It:

1. Runs the offline browser regression suite.
2. Sets up JDK 17 and Android SDK 35.
3. Builds the debug-signed APK.
4. Uploads workflow artifacts.
5. Publishes a numbered prerelease `prototype-v1.0.0-build.N`, attached to the tested commit, containing the APK, HTML and checksums.

The built-in Actions token needs **contents: write**, declared in the workflow. No signing or cloud secrets are required for these prototype builds. Repository/organization Actions policies must permit workflow execution and release creation.

## Tests

Test tooling is development-only and is not included in the HTML or APK:

```sh
npm install --no-save playwright@1.64.1 @sparticuz/chromium@153.0.0
node tests/prototype.cjs
```

The suite checks onboarding; streaming/stop; confirmed, cancelled and automatic actions; live timers; alarm validation; safe note rendering; history search; tool settings; memory and prompt persistence; model downloads, pause/resume and RAM errors; community search; Bangla labels; PIN flows; reload behavior; delete-all; mobile/desktop layout; denied storage; console exceptions; keyboard/visual viewport resizing; and network traffic limited to explicitly requested Hugging Face metadata. It runs against the file directly, with screenshots written into ignored `test-results/`.

Linux needs Chromium's usual shared libraries (available on GitHub's Ubuntu runner). For manual checks, also test Android keyboard behavior, hardware back, screen rotation and suspend/resume on a physical phone. Browser automation is not a substitute for device testing.

### Keyboard and live discovery regression checks

The browser suite intercepts the real Hugging Face API URLs with deterministic responses. It covers Enter/button submission, query encoding, HTTP/network failures, stale requests, GGUF-only filtering, missing sizes, and simulated downloads. Keyboard tests exercise both a resized content viewport and a visual-viewport-only resize. These tests do not prove physical-device IME behavior or live service availability; verify both on your Android phone.
