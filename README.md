# Hey Bharti (Android)

A privacy-first wake phrase prototype for POCO/Android 15.

## What it does
- Runs speech recognition only after the user opens the app and taps **Start listening**.
- Detects the phrase **Hey Bharti** and launches the ChatGPT app.
- Provides a visible stop control. The microphone is not secretly or continuously active.

## Important limitations
- Android does not permit an ordinary app to silently start microphone use from the background or after reboot.
- This prototype does not bypass the lock screen or keep a hidden always-on microphone.
- Launching ChatGPT does not guarantee that ChatGPT Voice Mode starts automatically; use ChatGPT's Voice button if it does not.
- Android speech recognition may send audio to the device's configured recognition service.

## Build
GitHub Actions builds a debug APK on every push to main. Open **Actions → Android APK Build → latest run → Artifacts** to download it.
