# Nothing風 ライブ壁紙 設計

## 背景

ランチャーアプリと同じデザインテイスト（黒背景・ミニマルな時計タイポグラフィ・カレンダーカード）の壁紙が欲しいという要望から着手。当初は静止画PNGとして生成したが、時刻が生成時点で固定されてしまい「常に現在時刻を表示したい」という要求を満たせないことが判明。Androidのロック画面はOS標準で静止画しか受け付けないことが多い（Samsung One UIも同様）ため、確実に動作する**ホーム画面向けのライブ壁紙**（`WallpaperService`）として実装する方針に転換した。ロック画面への適用は機種依存であり、対応を保証しない。

## スコープ

- ホーム画面用ライブ壁紙（`WallpaperService`）を1つ追加する
- 表示内容: 時計（`HH:mm`）＋日付/曜日＋カレンダーカード（月表記・曜日英語表記・日付数字）
- デザインはランチャー本体（`Clock.kt`、カレンダーウィジェット設計）と同じ配色・レイアウト言語を踏襲する

対象外:
- ロック画面への確実な適用（OSの制約により保証しない。設定できた場合の動作は副次的に許容するのみ）
- 予定データの表示（既存のカレンダーウィジェット設計と同様、日付情報のみ）
- 壁紙のカスタマイズ設定画面（配色・レイアウトの変更UIは作らない）

## 配色（既存の`Theme.kt`/`IconPalette.kt`と同一の値を使用）

- 背景: 黒 `#000000`
- 主要テキスト: 白 `#F5F5F5`
- 補助テキスト: グレー `#8A8A8A`
- アクセント: レッドオレンジ `#D1432B`
- カード枠線: ダークグレー `#333333`

Compose非依存の描画（`android.graphics.Canvas`）のため、上記の値を`wallpaper/NothingWallpaperColors.kt`に`android.graphics.Color`のARGB整数として改めて定義する（既存コードでも`Theme.kt`と`IconPalette.kt`で同じ配色を別々に定義しているのと同じパターンを踏襲し、新たな共有抽象は導入しない）。

## アーキテクチャ

- **`wallpaper/NothingWallpaperService.kt`**: `WallpaperService`を継承。`onCreateEngine()`で内部の`Engine`クラスを返す。
  - `Engine.onSurfaceChanged(holder, format, width, height)`: 画面サイズを保持し、レイアウト計算に使う（Fold機の展開/折りたたみによるサーフェスサイズ変化に自動追従）
  - `Engine.onVisibilityChanged(visible)`: `visible=true`で1秒間隔の再描画ループを開始、`false`で停止（バッテリー節約）
  - 再描画ループ: `Handler(Looper.getMainLooper())` + 自身を再投稿する`Runnable`
  - 描画処理: `SurfaceHolder.lockCanvas()` → `Canvas`に`Paint`で背景・時計・カレンダーカードを描画 → `unlockCanvasAndPost()`
- **`wallpaper/WallpaperTextFormat.kt`**: 時刻・日付・曜日の文字列フォーマットを行う純粋関数群（`java.util.Date` → `String`）。`Clock.kt`と同様の`SimpleDateFormat`ベース。
- **`wallpaper/NothingWallpaperColors.kt`**: 配色定数（`android.graphics.Color`のARGB整数）

## 描画レイアウト

- 上部: 大きく`HH:mm`（白、Light weight、`Typeface.create("sans-serif-light", Typeface.NORMAL)`）
- その下: 小さく`MM.dd EEE`（グレー）＋右にアクセント色の小さいドット
- さらに下: カレンダーカード（角丸矩形、黒地＋グレー枠線）
  - カード内上部: 月＋年（小・グレー、例 "SEPTEMBER 2026"）
  - その下: 曜日英語表記（大文字、アクセント色、例 "SUNDAY"）
  - その下: 日付数字を大きく白で
- 各要素のサイズ・余白は`Engine`が保持する画面幅を基準にした相対値で計算し、固定ピクセル値は使わない（画面サイズ非依存）

## エラーハンドリング

- 描画中の例外は`lockCanvas()`/`unlockCanvasAndPost()`のtry-finallyで囲み、キャンバスのロック解除漏れを防ぐ（クラッシュしても壁紙選択自体は他の壁紙にフォールバック可能なOS標準の挙動に任せる）
- それ以外の追加エラーハンドリングは行わない（個人利用アプリのためYAGNI）

## マニフェスト変更

- `AndroidManifest.xml`に`<service>`を追加:
  - `android:name=".wallpaper.NothingWallpaperService"`
  - `android:permission="android.permission.BIND_WALLPAPER"`
  - `android:exported="true"`
  - intent-filter: `android.service.wallpaper.WallpaperService`
  - meta-data: `android.service.wallpaper` → `res/xml/nothing_wallpaper.xml`（サムネイルdrawableと説明文字列を参照）
- `res/xml/nothing_wallpaper.xml`（新規）、`res/drawable/wallpaper_thumbnail.xml`（新規、簡易な黒背景＋時計モチーフのベクター）、`res/values/strings.xml`に説明文字列を追加

## テスト方針

- `wallpaper/WallpaperTextFormat.kt`の各フォーマット関数はJUnitでテスト（既存の`AppFilter`/`IconPalette`と同じ方針）。TDDで先にテストを書いてから実装する。
- `WallpaperService.Engine`のライフサイクル・実際の描画・1秒更新・Fold開閉時のリサイズ追従は実機で手動確認する（Compose UIテストと同様、個人アプリの規模的にUIの自動テストは見送り）

## 検討した代替案

- **静止画PNGを壁紙として設定**（当初案）: 実装コストが最も低いが、時刻が生成時点で固定されてしまい「常に現在時刻を表示したい」という要求を満たせないため不採用。
- **ロック画面への対応を前提にした実装**: AndroidおよびSamsung One UIではロック画面は基本的に静止画のみ対応というOS側の制約があり、ライブ壁紙をロック画面で確実に動かす手段がないため、ホーム画面向けとして割り切った。
