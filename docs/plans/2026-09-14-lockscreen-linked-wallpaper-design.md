# ロック画面連動カレンダー壁紙 設計

## 背景

既存の[ホーム画面ライブ壁紙](2026-09-13-nothing-live-wallpaper-design.md)は時計＋日付カードを毎秒描画しているが、ロック画面には反映されない。今回は「ロック画面とランチャーが連動した感じ」を出すため、ロック画面にも同じ日付カードを表示したいという要望。

Androidの制約上、サードパーティアプリのLive Wallpaperはロック画面(キーガード)の裏に描画できず、ロック画面壁紙は`WallpaperManager.setBitmap(..., FLAG_LOCK)`による**静止画**でしか設定できない。そのため「本当のLive」ではなく、**アプリをフォアグラウンドに復帰させたタイミングで日付カードを再描画した静止画を生成し、ロック画面壁紙として再設定する**方式を採る。夜間バッチ(WorkManager/AlarmManager)は導入しない(Doze/バッテリー最適化の影響を受けやすく、個人アプリの規模では過剰なため)。

## スコープ

- ロック画面壁紙に、ホーム画面ライブ壁紙と同じ「日付カード」(月＋年・曜日・日付数字)を静止画として設定する
- カード位置はホーム画面ライブ壁紙と同じ座標比率にし、ロック解除時に視覚的な連続性を出す
- カレンダーウィジェット長押しで表示するトグルでON/OFFを切り替える(デフォルトOFF、侵入的な壁紙上書きのため明示オプトイン)
- ON時: 即座に1回生成・適用。以後はアプリをフォアグラウンドに復帰させた時(`onResume`)のみ再生成・再適用

対象外:
- 夜間の自動更新(WorkManager/AlarmManager)
- ロック画面での時計表示(システム標準のロック画面クロックと重複するため描画しない)
- OFF時に既存のロック画面壁紙を元に戻す機能
- 専用のSettings画面(既存の長押しメニューのパターンを踏襲する)

## アーキテクチャ

### 共有ジオメトリの抽出(既存コードのリファクタ)

現在`NothingWallpaperService.Engine.render()`にベタ書きされている日付カードの位置計算を、Canvas非依存の純粋関数として抽出する。

- **`wallpaper/NothingWallpaperRenderer.kt`**(新規): `calculateCardRect(width: Float, height: Float): RectF`。既存の`render()`内の`cardTop`/`cardHeight`/`margin`計算をそのまま移設(`CalendarMath.kt`と同じ「Android非依存の純粋関数」パターン)。
- `NothingWallpaperService.Engine.render()`はこの関数を呼ぶようリファクタ。描画結果・見た目は変更しない。

### 新規: ロック画面用ジェネレータ

- **`wallpaper/LockWallpaperGenerator.kt`**(新規): 以下を行う関数を持つ
  1. `WallpaperManager`の`desiredMinimumWidth`/`desiredMinimumHeight`でBitmapサイズを決定
  2. `Bitmap` + `Canvas`に、背景(黒)＋日付カード(月年・曜日・日付数字)のみを描画。時計・上部日付行(`HH:mm`や`MM.dd EEE`)は描かない
  3. カード位置は`NothingWallpaperRenderer.calculateCardRect()`を使い、ホーム画面ライブ壁紙と同じ比率にする
  4. `WallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)`を呼ぶ
  5. 例外はtry/catchで囲み、失敗時は無視(ログのみ)

### トリガー

- `LockScreenSyncStore.isEnabled()`が`true`の場合、`MainActivity.onResume()`から`LockWallpaperGenerator.apply(context)`を呼ぶ

### 設定の永続化

- **`data/LockScreenSyncStore.kt`**(新規): `HiddenAppsStore`と同じSharedPreferencesパターンで`enabled: Boolean`を保存

## UI

- `HomeScreen.kt`のカレンダーウィジェット(`CalendarWidget`)を長押しすると、既存のアプリ長押しメニューと見た目を揃えたポップアップ/ボトムシートを表示
- 中身は「ロック画面と連動」のトグル1つのみ
- ONにした瞬間、`LockScreenSyncStore`に保存＋即座に1回`LockWallpaperGenerator.apply()`を実行
- OFFにした場合は保存のみ(既に設定済みのロック画面壁紙はそのまま残す)

## マニフェスト変更

- `AndroidManifest.xml`に`<uses-permission android:name="android.permission.SET_WALLPAPER" />`を追加(normal permission、ランタイム許可ダイアログ不要)

## エラーハンドリング

- `LockWallpaperGenerator`内の`WallpaperManager.setBitmap`呼び出しはtry/catchで囲み、失敗時はログのみで無視。トグルのON/OFF状態は呼び出し成否に関わらず保存する
- それ以外の追加エラーハンドリングは行わない(個人利用アプリのためYAGNI)

## テスト方針

- `NothingWallpaperRenderer.calculateCardRect()`はJUnitで純粋関数テスト(`CalendarMathTest.kt`と同じ方針、TDDで先にテストを書く)
- ホーム画面ライブ壁紙のリファクタ後も見た目が変わらないことは実機で目視確認
- ロック画面への実際の反映(長押し→トグルON→ロック画面確認)は実機で手動確認。Compose UIテスト・Instrumentedテストは個人アプリの規模的に見送る

## 検討した代替案

- **ホーム画面Live Wallpaperの強化のみ(ロック画面対応を見送る)**: 「連動した感じ」を出す本来の目的を満たせないため不採用。
- **AlarmManager/WorkManagerでの夜間自動更新**: Doze/バッテリー最適化の影響を受けやすく実装・検証コストが高いため、今回は見送り。フォアグラウンド復帰時の更新のみで十分と判断。
