# TabataTimer

*A Tabata / HIIT interval timer for Android, with cloud-synced workout history.*

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?logo=materialdesign&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase&logoColor=black)

TabataTimer runs high-intensity interval training (HIIT) workouts: you set your work and rest
durations and the number of rounds, and the app counts you through the session with audio cues.
Completed workouts are saved to the cloud so you can review your history across sessions.

> Built as a coursework project for a Mobile Systems & Applications (SMA) module.

<!-- Tip: a screen recording or screenshot of the timer really helps here.
     ![TabataTimer](docs/screenshot.png) -->

## Features

- **Configurable intervals** — set work time, rest time, and number of rounds.
- **Interval "horn" cue** — optional audible signal at a chosen interval during the work phase.
- **Full timer controls** — start, pause/resume, and reset, with a clear work/rest phase indicator.
- **Audio feedback** — sound cues on phase changes via Android `SoundPool`.
- **Workout history** — every completed session is saved per user and can be listed back.

## Architecture

The app follows **MVVM** with Jetpack Compose:

- **`TabataViewModel`** holds the workout state machine — `IDLE / RUNNING / PAUSED` states and
  `WARMUP / WORK / REST` phases — driven by a `CountDownTimer`, with UI state exposed via Compose
  `mutableState`.
- **`SoundManager`** wraps `SoundPool` for low-latency playback of the horn cue.
- **Firebase** provides **Anonymous Authentication** (so a user has an identity with no sign-up
  friction) and **Cloud Firestore** for persisting each workout under `users/{uid}/workouts`.

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose**
- **Firebase** — Anonymous Auth + Cloud Firestore
- `minSdk 24`, `targetSdk 36`

## Getting started

### Prerequisites
- Android Studio
- A Firebase project (for auth + Firestore)

### Setup

1. Clone the repository and open it in Android Studio.
2. Create a Firebase project, enable **Anonymous Authentication** and **Cloud Firestore**, and
   replace `app/google-services.json` with the config file from *your* project.
3. Build and run on an emulator or device.

## Project structure

```
app/src/main/java/com/example/tabatatimer/
├── MainActivity.kt        # Entry point; wires up Firebase auth and the ViewModel
├── TabataViewModel.kt     # Timer state machine + Firestore history
├── TabataScreen.kt        # Compose UI
├── SoundManager.kt        # SoundPool audio cues
└── ui/theme/              # Material 3 theme
```
