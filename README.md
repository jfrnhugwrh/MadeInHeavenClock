# Made in Heaven Clock

A Jetpack Compose Android clock with a time-acceleration gimmick inspired by Enrico Pucci's
Stand, **Made in Heaven** (JoJo's Bizarre Adventure: Stone Ocean).

## Features

- Accurate real-time clock on launch: analog face, digital HH:mm:ss (follows the system 12/24h
  setting) and the full localized date including the year.
- **ACCELERATE TIME** button: speed ramps exponentially from x1 to x1,000,000,000 over 10 s
  (about 15 years skipped during the ramp, then ~32 years per second at the cap).
- FX that scale with the speed: radial speed lines, sky shifting from indigo to blood red and
  gold, slow colour-negative pulses, screen shake, menacing ゴゴゴゴ glyphs, a live speed
  counter, a progress bar and a "time skipped" readout.
- Procedural tick audio (no assets): tick rate and pitch climb with the speed until the ticks
  fuse into a rising whine. SFX can be muted.
- **RESET UNIVERSE** button: white flash, the clock visibly rewinds to the real present and the
  counter spins back down to x1. Reaching the year 9999 triggers the reset automatically (the loop).

## Build and run

Requirements: Android Studio (Ladybug 2024.2+ recommended), JDK 17 (bundled with Studio),
an Android 8.0+ (API 26) device or emulator.

### Android Studio (easiest)

1. File > Open > select the `MadeInHeavenClock` folder.
2. Let Gradle sync finish. Studio downloads Gradle 8.9 and any missing SDK pieces.
   If it offers an AGP/Kotlin upgrade, accepting it is fine.
3. Pick a device (emulator via Device Manager, or a phone with USB debugging enabled).
4. Press Run.

### Command line

The Gradle wrapper scripts and JAR are not included (they are binary/generated files).
Create them once with an installed Gradle, or simply open the project in Android Studio:

```
gradle wrapper --gradle-version 8.9
gradlew.bat assembleDebug     (macOS/Linux: ./gradlew assembleDebug)
gradlew.bat installDebug      (installs on the connected device/emulator)
gradlew.bat test              (unit tests)
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`; you can also
`adb install -r` it manually. `local.properties` with your `sdk.dir` is written
automatically by Android Studio.

## Tuning

All the knobs live in `model/TimeAcceleration.kt`: `MAX_SPEED`, `RAMP_SECONDS`,
`RESET_SECONDS`, `MAX_YEAR`.

## Architecture

```
MainActivity.kt                 lifecycle wiring (time loop + audio start/stop)
model/ClockViewModel.kt         state machine NORMAL -> ACCELERATING -> UNIVERSE_RESET, StateFlow
model/TimeAcceleration.kt       pure speed curve / rewind maths (unit tested)
model/ClockFormatters.kt        pure formatting (unit tested)
model/ClockUiState.kt           immutable per-frame UI state
audio/TickSynth.kt              streaming AudioTrack synthesizer
ui/ClockScreen.kt               stateless root screen, portrait + landscape
ui/components/                  AnalogClockFace, DigitalReadout, SpeedLines, SpeedMeter, ControlPanel
ui/theme/                       Material theme + per-frame HeavenPalette
```

Coroutines: the ~60 fps loop runs inside `repeatOnLifecycle(STARTED)`, so it is cancelled when
the app is backgrounded. In normal mode the clock reads the system time every frame (no
drift); in accelerated mode a virtual clock advances by `realDelta * speed`.

## Safety note

Inversion pulses are capped below 1 Hz cycles and the reset flash is a single fade, but
the app still contains intense motion and flashing colours. Mute and reset are always one tap away.
