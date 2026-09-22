# ホーム画面への今日のモバイル通信量バッジ追加

## 背景

[[2026-09-22-home-knob-signal-badge-design]] で追加した `SignalBadge`（右上の電波強度dBm表示）に続く
「Nothingらしい隠れた数値」路線の第2弾として、今日のモバイル通信量（RX+TX合計、深夜0時リセット）を
表示したい。検索画面のジョグホイールにある `SystemStatsWidget` のNET欄は瞬間の通信速度(↑↓)であり、
今回追加するのは別軸の「今日ここまでの積算量」。

## 方針

### データ取得・永続化

正確な期間集計ができる `NetworkStatsManager` は `Settings.ACTION_USAGE_ACCESS_SETTINGS`
（設定アプリでアプリ一覧からこのランチャーを探して手動でONにする、通常の実行時許可ダイアログより
摩擦の大きい特別なアクセス許可）が必要なため見送り、新規パーミューション不要な
`TrafficStats.getMobileRxBytes() + getMobileTxBytes()`（端末起動からの累積値）を深夜0時基準で
差分計算する方式を採る。

日付またぎ・再起動検知のロジックを `StatusMath.kt` に純粋関数として切り出す:

```kotlin
data class DailyUsageResult(val usageBytes: Long, val newBaselineBytes: Long, val newBaselineDate: String)

fun dailyMobileUsage(currentTotalBytes: Long, baselineBytes: Long?, baselineDate: String?, today: String): DailyUsageResult
```

- `baselineDate` が今日と異なる（日付が変わった） → ベースラインを現在値にリセット、今日の使用量は0から
- 現在値 < 保存済みベースライン（再起動でカウンタがリセットされた） → ベースラインを0とみなし、
  現在値をそのまま「再起動後〜今」の使用量として扱う（再起動前〜深夜0時分は取りこぼす。許容する
  エッジケース）
- それ以外（同日・再起動なし） → 現在値−ベースラインが今日の使用量

ベースライン値・日付は既存の `LockScreenSyncStore` と同じ `SharedPreferences` パターンで新規
`MobileDataBaselineStore` に保存する。既存の `StatusIconsController` が network 系の `StateFlow`
（`wifiConnected`/`signalBars`/`signalDbm`等）を一手に引き受けているため、この
`MobileDataBaselineStore` も `StatusIconsController` 内部だけで使う実装詳細とし、MainActivityへは
配線しない。1分おきのコルーチンで再計算し `dailyMobileDataUsage: StateFlow<Long>`（バイト単位）として
公開する。新規パーミッション不要。

### UI・視覚表現

新規 `ui/MobileDataBadge.kt` を作成する。見た目は `SignalBadge` と同じピル形状
（`NothingGrays.Base`背景、`RoundedCornerShape(50)`）を踏襲するが、色変化の閾値ロジックは付けない
（データ量に「弱い/強い」の二値の意味がないため、常に白文字）。

バイト値をMB単位に変換して整数表示（例: `342MB`）。1000MB以上は`1.2GB`のようにGB表記へ切り替える
閾値ロジックを `StatusMath.kt` に純粋関数として追加する。

`HomeScreen.kt` 側は既存の `SignalBadge` を `Column` でラップし、その直下に `MobileDataBadge` を
`Arrangement.spacedBy(4.dp)` 程度の間隔で縦に並べる。位置は `SignalBadge` と同じ `TopEnd` +
`gridEdgeInset` を共有する。値が取得できない場合はdBmバッジ同様に非表示にする。

### 検討した代替案

- `NetworkStatsManager` による正確な期間集計 → 見送り（Usage Access許可の摩擦が大きいため）
- RX/TXを分けて表示 → 見送り（ギガバイト制限を意識する用途では合計値の方が知りたい数値に近いため）
- Wi-Fi分も表示 → 見送り（モバイル回線のみに絞ることで「ギガ数を気にする」用途に特化）
- モバイル未使用日はバッジ自体を非表示 → 見送り（0MBも「今日はまだモバイル使ってない」という情報
  として意味があるため、常に表示する）

## 変更対象

- 新規 `data/MobileDataBaselineStore.kt`: `LockScreenSyncStore` と同じ `SharedPreferences` パターンで
  ベースラインバイト数・日付を保存
- `StatusMath.kt`: `dailyMobileUsage()`（日付またぎ・再起動判定）と、MB/GB表示切り替えの閾値ロジックを
  純粋関数として追加
- `StatusIconsController.kt`: `MobileDataBaselineStore` を内部で保持し、1分おきのコルーチンで
  `TrafficStats` を読み `dailyMobileUsage()` に通して `_dailyMobileDataUsage: MutableStateFlow<Long>`
  を更新
- 新規 `ui/MobileDataBadge.kt`: バイト値を受け取りピル形状バッジ（`SignalBadge`と同スタイル、色変化なし）
  を描画するComposable
- `MainActivity.kt`: `dailyMobileDataUsage` を `collectAsState()` して `HomeScreen.kt` に渡す
- `HomeScreen.kt`: `SignalBadge` を `Column` でラップし、直下に `MobileDataBadge` を追加

## テスト

`dailyMobileUsage()`（同日通常ケース・日付またぎ直後・再起動直後の3パターン）と、MB/GB表示切り替えの
閾値ロジック（境界値999MB/1000MB）を `StatusMathTest.kt` に追加してユニットテスト化する。
`MobileDataBaselineStore` の読み書き自体は既存の `LockScreenSyncStore` 同様、新規テストは追加しない。
UI自体（バッジの位置・レイアウト）は目視確認とする。
