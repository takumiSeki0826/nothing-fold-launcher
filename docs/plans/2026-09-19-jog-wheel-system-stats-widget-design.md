# ジョグホイール右上のシステムステータスウィジェット 設計

## 背景

展開時（Galaxy Z Fold8を開いた状態）にドロワーを開くと、左側にアルファベットジョグホイール（`AlphabetJogWheel.kt`）が表示される。ホイールを囲むCLR/TOP/END/REVの4つのボタンは左上・左下・右下に配置されており、右上だけ空きスペースになっている。ここにCPU/メモリ/ネットワーク/ストレージの使用状況を表示する「かっこいい」ステータスウィジェットを追加したいという要望。

## スコープ

- `AlphabetJogWheel.kt`の`Box`内、`Alignment.TopEnd`に新規コンポーネント`SystemStatsWidget`を追加する
- 表示は常時（ジョグホイールが表示されている間＝展開時にドロワーを開いている間）で、条件分岐は不要
- 表示項目: CPU使用率・メモリ使用率・ストレージ使用率・ネットワーク速度（上り/下り）の4種5行

## ビジュアルデザイン

```
CPU  ●●●○○  23%
MEM  ●●●●○  61%
STO  ●●●●○  78%
NET ↑    12KB/s
NET ↓   340KB/s
```

- 既存の`WeatherWidget`/`StatusIcons`と同じ配色・フォント規約（`FontFamily.Monospace`、`NothingGrays`）を踏襲する
- CPU/MEM/STOの3行: ラベル（10sp グレー）＋5segドットゲージ＋数値（12sp 白）を横並びで1行にする
  - ドットゲージは`StatusIcons.kt`の`SignalBarsIcon`と同じ描画方式（`Canvas`で`drawRoundRect`を5つ並べる）を流用する
  - 塗り＝白、未点灯＝`NothingGrays.Base`
  - 使用率90%以上の項目は、そのゲージのみアクセント赤（`#D1432B`）に切り替える（`BatteryIcon`の低残量時の赤と同じ警告表現）
- NET↑/NET↓の2行: ゲージなし、ラベル＋数値のみ（スループットは上限のない値のためゲージ化しない）
  - 単位は`KB/s`/`MB/s`をしきい値で自動切り替え

## アーキテクチャ

### SystemStatsController.kt（新規）

既存の`StatusIconsController`と同じ`register()`/`unregister()` + `StateFlow`公開パターンに揃える。

- `register()`内でコルーチンスコープを開始し、2秒間隔（`delay(2_000)`）のポーリングループで4項目すべてを更新する
- `unregister()`でスコープをキャンセルする
- `MainActivity`で`remember { SystemStatsController(applicationContext) }` + `DisposableEffect`で登録する（`StatusIconsController`と同じ配線）

### データ取得方法

| 項目 | 取得方法 | 権限 |
|---|---|---|
| CPU% | `/proc/stat`を2回サンプリングし、差分からidle/total比率を算出 | 不要 |
| メモリ% | `ActivityManager.getMemoryInfo()`の`availMem`/`totalMem` | 不要 |
| ストレージ% | `StatFs`で内部ストレージの空き/合計を取得 | 不要 |
| ネット速度 | `TrafficStats.getTotalRxBytes()`/`getTotalTxBytes()`を2秒間隔でサンプリングし差分から算出 | 不要（`ACCESS_NETWORK_STATE`は既存） |

### エラーハンドリング

- `/proc/stat`が読めない端末（一部OEMで制限される場合がある）では、CPU%のみ「--」表示にしてクラッシュさせない
- 他の3項目（メモリ・ストレージ・ネット）とは独立して失敗を扱い、一つの取得失敗が他に影響しないようにする（既存`StatusIconsController`と同じ方針）

## テスト方針

- CPU%算出・メモリ%算出・ストレージ%算出・速度フォーマット（KB/s⇔MB/s切り替え、90%しきい値判定）を純粋関数として`SystemStatsMath.kt`に切り出し、`SystemStatsMathTest.kt`でユニットテストする
- ドットゲージの見た目・実際のシステム値の妥当性は実機で目視確認する
