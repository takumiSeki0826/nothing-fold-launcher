# nothing-fold-launcher

**English** | [日本語](#日本語)

A self-made Android launcher for the Galaxy Z Fold8 with a minimal, dot-matrix look inspired by Nothing Phone. It supports both the folded (cover screen) and the unfolded (expanded) layouts.

## Features

- **Home screen**: clock, weather, calendar, now playing, volume/brightness sliders, folder knobs and an app grid
  - **Cover screen (folded)**: clock → calendar / now playing cards → sliders + a two-row app grid, stacked vertically with fixed spacing and centered
  - **Calendar**: a dot-drawn monthly calendar. The header reads like `TUE 10.06`, and only today's dot lights up in the accent color
  - **Now playing**: title and artist, plus a dot equalizer that moves only while playing. The top-right button toggles play/pause; tapping the card opens the source app
- **Expanded layout (Fold8 unfolded)**: the left panel holds the clock, weather, calendar and media info; the right panel holds the sliders, knobs and app grid. Card contents are drawn a bit smaller
- **App drawer**: when unfolded, an alphabet jog wheel scrubs A–Z; search and favorites are supported
  - Tapping the center (white spindle) of the jog wheel toggles music play/pause, with a dot play/pause icon shown there. The wheel spins while music plays
  - A system status widget sits at the top right of the jog wheel (CPU / memory / storage usage, signal strength in dBm, today's mobile data usage, network up/down speed). Tapping the network row opens speedtest.net
- **Folders**: folders can be created on the home knobs and app grid. Apps inside a folder show an orange dot on their icon
- **Custom status bar**: battery, Wi-Fi, mobile signal strength and VPN/Tailscale badges are drawn by the app (the OS status bar is hidden)
- **Live wallpaper**: a dot-calendar lock screen wallpaper linked to the home screen
- **App management**: hide, rename and uninstall apps; pick a color palette for app icons

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- Kotlin Coroutines / StateFlow
- Weather comes from [Open-Meteo](https://open-meteo.com/) (no API key needed), using the device's current location
- minSdk 33 / targetSdk 37 / compileSdk 37
- JVM 17

## Build and run

### Prerequisites

- JDK 17
- Android SDK (`adb`)
- A real device is recommended: Galaxy Z Fold8 (needed to check both the unfolded and folded layouts)

### Commands

```bash
# Unit tests
./gradlew :app:testDebugUnitTest

# Debug build
./gradlew :app:assembleDebug

# Install on a device / emulator
./gradlew :app:installDebug
```

After installing, choose this launcher as the default home app in Android settings.

## Project structure

```
app/src/main/java/com/sekitakumi/nothingfoldlauncher/
├── MainActivity.kt               # entry point; wires up navigation and state
├── NowPlayingListenerService.kt  # notification listener that reads now-playing media
├── data/                         # persistence for apps, folders, favorites and settings
├── ui/                           # Composable screens, widgets and pure functions (*Math.kt)
└── wallpaper/                    # live wallpaper linked to the home / lock screen
```

Grays are unified under the `NothingGrays` tokens in `ui/theme/Color.kt` (see the project's `CLAUDE.md`, written in Japanese, for details).

## Permissions

- Notification access: reads now-playing media (`NowPlayingListenerService`)
- Location (`ACCESS_FINE_LOCATION`): fetches the weather
- Phone state (`READ_PHONE_STATE`): shows mobile signal strength and network type
- Network state / Wi-Fi state / Internet: status display, weather requests and data usage tracking
- Set wallpaper (`SET_WALLPAPER`): links the lock screen wallpaper
- Delete packages (`REQUEST_DELETE_PACKAGES`): uninstalls apps from the app drawer
- Runs as a home app and also provides a live wallpaper service

## Design documents

For each feature, a dated Markdown design doc covering background, scope, architecture and test plan is kept in `docs/plans/` (written in Japanese).

## Testing

Pure functions such as `ui/*Math.kt` are unit-tested test-first (`app/src/test/`). Composable visuals and real system behavior (sensor values, permission flows, etc.) are checked by hand on a real device.

## Notes

This is a personal app, tuned for the **Galaxy Z Fold8**. The layout may break on other devices.

## License

[MIT License](LICENSE)

---

# 日本語

[English](#nothing-fold-launcher) | **日本語**

Galaxy Z Fold8向けに自作している、Nothing Phone風のミニマル・ドットマトリクス美学を持つAndroidランチャー。折りたたみ(カバー画面)・展開時の両方のレイアウトに対応する。

## 特徴

- **ホーム画面**: 時計・天気・カレンダー・再生中メディア・音量/明るさスライダー・フォルダノブ・アプリグリッド
  - **カバー画面(折りたたみ時)**: 時計 → カレンダー/再生中の2カード → スライダー + アプリグリッド(2段)を縦に並べ、ブロック間は固定の余白で縦中央に配置
  - **カレンダー**: ドットで描いた月間カレンダー。見出しは `TUE 10.06` 形式で、今日の日付だけアクセント色で光る
  - **再生中メディア**: 曲名・アーティスト名と、再生中だけ動くドットのイコライザー。右上のボタンで再生/停止、カードのタップで再生元アプリを開く
- **展開時(Fold8を開いた状態)専用レイアウト**: 左パネルに時計・天気・カレンダー・メディア情報、右パネルにスライダー・ノブ・アプリグリッドを配置。カード内の中身は一回り小さく表示する
- **アプリドロワー**: 展開時はアルファベットジョグホイールでA-Zスクラブ、検索、お気に入り表示に対応
  - ジョグホイール中央(白い軸)をタップすると音楽の再生/停止を切り替え、中央には再生/停止のドットアイコンを表示。再生中はホイールが回転する
  - ジョグホイール右上にシステムステータスウィジェット(CPU/メモリ/ストレージ使用率、電波強度(dBm)、本日のモバイルデータ使用量、ネットワーク上り/下り速度)を表示。ネットワーク行はタップでspeedtest.netを開く
- **フォルダ機能**: ホームノブ/アプリグリッド上にフォルダを作成可能。フォルダに入っているアプリのアイコンにはオレンジのドットを表示
- **カスタムステータスバー**: バッテリー残量・Wi-Fi・モバイル電波強度・VPN/Tailscaleバッジを独自描画(OS標準ステータスバーは非表示)
- **ライブ壁紙**: ホーム画面と連動したドットカレンダーのロック画面壁紙
- **アプリ管理**: 非表示・リネーム・アンインストール、アプリアイコンのカラーパレット選択

## 技術スタック

- Kotlin + Jetpack Compose (Material 3)
- Kotlin Coroutines / StateFlow
- 天気は [Open-Meteo](https://open-meteo.com/) (APIキー不要)。位置情報は端末の現在地を使う
- minSdk 33 / targetSdk 37 / compileSdk 37
- JVM 17

## ビルド・実行

### 前提

- JDK 17
- Android SDK (`adb`)
- 実機推奨: Galaxy Z Fold8(展開/折りたたみ両レイアウトの確認に実機が必要)

### コマンド

```bash
# ユニットテスト
./gradlew :app:testDebugUnitTest

# デバッグビルド
./gradlew :app:assembleDebug

# 実機/エミュレータにインストール
./gradlew :app:installDebug
```

インストール後、Androidの設定からデフォルトのホームアプリとしてこのランチャーを選択する。

## プロジェクト構成

```
app/src/main/java/com/sekitakumi/nothingfoldlauncher/
├── MainActivity.kt               # エントリーポイント、画面遷移・状態管理の配線
├── NowPlayingListenerService.kt  # 再生中メディア情報取得用の通知リスナー
├── data/                         # アプリ一覧・フォルダ・お気に入り・各種設定の永続化層
├── ui/                           # Composable画面・ウィジェット・純粋関数(*Math.kt)
└── wallpaper/                    # ホーム/ロック画面と連動するライブ壁紙
```

配色は`ui/theme/Color.kt`の`NothingGrays`トークンに統一している(詳細はプロジェクトの`CLAUDE.md`を参照)。

## 権限

- 通知へのアクセス: 再生中メディアの取得(`NowPlayingListenerService`)
- 位置情報(`ACCESS_FINE_LOCATION`): 天気の取得
- 電話の状態(`READ_PHONE_STATE`): モバイル電波強度・ネットワーク種別の表示
- ネットワーク状態 / Wi-Fi状態 / インターネット: ステータス表示、天気取得、通信量の集計
- 壁紙の設定(`SET_WALLPAPER`): ロック画面壁紙との連動
- アプリの削除(`REQUEST_DELETE_PACKAGES`): アプリドロワーからのアンインストール
- ホームアプリとして動作し、ライブ壁紙サービスも提供する

## 設計ドキュメント

機能追加のたびに、背景・スコープ・アーキテクチャ・テスト方針をまとめた設計docを`docs/plans/`に日付付きMarkdownで残している。

## テスト方針

`ui/*Math.kt`などの純粋関数はTDDでユニットテスト(`app/src/test/`)を書く。Composableの見た目や実際のシステム挙動(センサー値・権限フローなど)は実機で確認する方針。

## 注意

個人用に作っているアプリで、**Galaxy Z Fold8を前提**にレイアウトを調整している。他の端末では表示が崩れる可能性がある。

## ライセンス

[MIT License](LICENSE)
