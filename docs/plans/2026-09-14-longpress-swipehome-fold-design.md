# Long-press YouTube Music, swipe-to-home, and Fold expanded layout

## Overview

Three independent improvements:

1. Long-pressing the Now Playing widget launches YouTube Music directly.
2. Swiping the OS home gesture while the search screen (AppDrawer) is open closes the drawer and returns to the home screen.
3. When the device is in an expanded/unfolded state (e.g. Galaxy Z Fold 8 opened), the home screen switches to a two-column layout.

## 1. Long-press Now Playing widget → launch YouTube Music

**Files**: `ui/NowPlayingWidget.kt`, `ui/HomeScreen.kt`, `MainActivity.kt`

- Change `NowPlayingWidget`'s root `Column` modifier from `clickable` to `combinedClickable(onClick, onLongClick)`.
- Add a new `onLongClick: () -> Unit` parameter, threaded through `HomeScreen` from `MainActivity`.
- Add `launchYoutubeMusic()` in `MainActivity`: resolve the launch intent for package `com.google.android.apps.youtube.music`. If found, start it with `FLAG_ACTIVITY_NEW_TASK`. If not installed, do nothing (no toast, no Play Store fallback).
- Existing tap behavior (`onClick` → `launchNowPlayingApp`, which launches whatever app is currently reporting playback) is unchanged.

## 2. Swipe OS home gesture closes the search screen

**Files**: `MainActivity.kt`

- The "swipe navigation bar from bottom to top" gesture referenced by the user is the OS-level home gesture (swiping up from the gesture pill), not an in-app swipe. The existing `detectDragGestures` in `MainActivity` (open drawer on swipe up, close on swipe down/left within the app) is unrelated and unchanged.
- `MainActivity` already uses `launchMode="singleTask"`, so a subsequent home-gesture launch intent should arrive via `onNewIntent`. Override `onNewIntent(intent: Intent)`: when `intent.action == Intent.ACTION_MAIN` and the drawer is currently open, close it (set drawer-open state to `false`).
- The drawer-open state (`showDrawer`, currently a local `remember { mutableStateOf(false) }` inside the composable) needs to become reachable from `onNewIntent`. Approach: hoist it to a `mutableStateOf<Boolean>` held on `MainActivity` (created once, e.g. via `mutableStateOf(false)` as a property) and pass it into the composable, instead of `remember`-ing it locally.
- **Known risk**: when an Activity is already in the foreground, Android may not always redeliver `onNewIntent` for a home-gesture on some OEM/OS combinations, since the system may treat the launcher as already "home". This needs on-device verification. If `onNewIntent` doesn't fire reliably, fall back to `onUserLeaveHint()` or `onPause()` to close the drawer instead — since for a launcher, leaving the drawer visible when the user departs the app is never desired regardless of the cause.

## 3. Fold expanded layout (two-column home screen)

**Files**: `app/build.gradle.kts`, `HomeScreen.kt`, `MainActivity.kt`

- Add a window-size-class dependency (`androidx.compose.material3:material3-window-size-class` or equivalent) to `app/build.gradle.kts`.
- In `MainActivity`, compute `calculateWindowSizeClass(this)` and pass whether the width class is `Expanded` (≥ 600dp) down into `HomeScreen` as a boolean (e.g. `isExpandedWidth`).
- In `HomeScreen`, branch on `isExpandedWidth`:
  - **Compact (folded/phone width)**: keep the existing single-column layout unchanged (Clock+StatusIcons row, Calendar+NowPlaying row, sliders+app grid row).
  - **Expanded (unfolded)**: switch to a two-column `Row`:
    - **Left column**: `Clock` + `StatusIcons`, `CalendarWidget`, `NowPlayingWidget`, `VolumeSlider` + `FaderSlider`, stacked vertically.
    - **Right column**: the app grid, using a wider `HOME_GRID_COLUMNS` (e.g. 6–8 instead of 4) to make better use of the space.
  - Shared pieces (Clock, CalendarWidget, NowPlayingWidget, sliders, app grid) are extracted into private composables reused by both layout branches to avoid duplication.
- `AppDrawer` (search screen) is explicitly out of scope for this change — it keeps its current single layout regardless of window size. Expanded-width support for AppDrawer may be addressed separately later.

## Testing

- Long-press: manual verification on a device/emulator with and without YouTube Music installed.
- Swipe-to-home: manual verification of the home gesture with the drawer open; verify `onNewIntent` fires as expected, adjust to the `onUserLeaveHint` fallback if not.
- Fold layout: verify via the Android Studio resizable/foldable emulator profile (Z Fold-like), checking both folded and unfolded width configurations render correctly.
