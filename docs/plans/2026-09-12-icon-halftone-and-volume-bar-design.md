# アイコンのハーフトーン化 ＋ 音量バー追加 設計

## 背景

MVPリリース後、実機確認でアプリアイコンの見た目（グレースケール＋丸マスク）が「汚い/丸まらない」と不評だった。またNothing Phone風の演出として、音量調整バーのような常時表示のドット要素も追加したい。

## スコープ

1. アプリアイコンをハーフトーン風のドット絵に変更する
2. ホーム画面上部に、実際にメディア音量を操作できるドット柄の横長音量バーを常時表示する

## アイコン描画（`ui/IconRenderer.kt`）

- 既存のグレースケール＋丸マスク処理を撤去し、ハーフトーン処理に置き換える
- 元アイコンを8x8グリッドに縮小し、各セルの輝度を計算
- 明るいセルほど大きい白ドット、暗いセルほど小さい白ドットを黒背景上に描画（最暗部でも完全には消えない最小ドットを残す）
- 輝度→ドット半径比率の変換は `ui/DotMath.kt` に純粋関数 `dotRadiusRatio(luminance: Float, minRatio: Float, maxRatio: Float): Float` として切り出し、JUnitでテストする
- 変換失敗時のプレースホルダーは既存のドット柄をそのまま流用

## 音量バー

- **`ui/VolumeController.kt`**: `AudioManager`（`STREAM_MUSIC`）をラップし、現在音量を`StateFlow<Float>`（0〜1の比率）で公開。`setRatio()`で音量を設定。`VOLUME_CHANGED_ACTION`のブロードキャストを受けて、ハードウェア音量ボタンでの変更にもStateFlowを追従させる。Activityの`onResume`/`onPause`で登録・解除する
- **`ui/VolumeBar.kt`**: 画面上部に常時表示する横長のドットバー（20個程度のドット、比率に応じて左から点灯）。ドラッグ操作で比率を計算し`VolumeController.setRatio()`を呼ぶ
- 比率⇔ドット数、ドラッグ位置⇔比率の変換は `ui/DotMath.kt` に純粋関数 `litDotCount(ratio, totalDots)` / `dragPositionToRatio(x, width)` として切り出しテストする
- `HomeScreen.kt`にVolumeBarを追加（時計の上）。`MainActivity.kt`でVolumeControllerを生成し、ライフサイクルに応じて登録/解除する

## エラーハンドリング

- `AudioManager`取得失敗時は操作不可の比率0固定にフォールバックし、クラッシュさせない
- アイコン変換失敗時は既存通りプレースホルダーを表示

## テスト方針

- `DotMathTest.kt`で`dotRadiusRatio`/`litDotCount`/`dragPositionToRatio`の境界値・丸めをJUnitでテスト
- 実機で以下を手動確認:
  - アプリアイコンがドット絵（ハーフトーン）になっていること
  - 音量バーをドラッグすると実際にメディア音量が変わること
  - 物理音量ボタンを押してもバーが追従すること
