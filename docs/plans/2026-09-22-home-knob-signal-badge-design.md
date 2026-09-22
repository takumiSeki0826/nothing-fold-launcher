# ホーム画面ノブへの電波強度(dBm)バッジ追加

## 背景

ホーム画面（展開/横並びレイアウト時のみ）には `RotaryKnob` が2つ（`HOME_1`/`HOME_2`）
右側に並んでいる。右側の `HOME_2` ノブの右上に、Nothingらしい「普段は見えない数値」を
覗かせる小さなバッジとして電波強度（dBm）を表示したい。

当初はバッテリー温度を検討したが、既にバッテリー%が `StatusIcons` にあるため見送り、
代わりに電波強度の生dBm値を採用する。既存の `StatusIcons` の電波アイコンは
バケツ化された0〜4のバー数（`level`）しか使っておらず、生のdBm値は取得も表示もしていない。

## 方針

`StatusIconsController.kt` が既に購読している `TelephonyManager` の
`SignalStrengthsListener`（`onSignalStrengthsChanged`）から、これまで捨てていた
`SignalStrength.getCellSignalStrengths()` の `getDbm()` を新たに読み取り、
`_signalDbm: MutableStateFlow<Int?>` として公開する。Wi-Fi接続中
（既存の `wifiConnected` フラグで判定）は新規に扱う `WifiManager.getConnectionInfo().rssi`
を優先的に使い、取得できなければセルラーdBmにフォールバック、どちらも取れなければ
バッジ自体を表示しない。

`RotaryKnob` に任意パラメータ `badgeContent: (@Composable () -> Unit)? = null` を追加し、
描画本体の `Canvas`（丸)だけを `Box` でラップして `Alignment.TopEnd` に重ねる
（ラベルのテキスト行は `Column` の外に残すため、ラベル幅に引っ張られてバッジ位置がずれない）。
`HomeScreen.kt` 側は `HOME_2` の呼び出し時のみこのパラメータを渡す。

バッジの見た目は `AppIconDot.kt` の `Box + background + CircleShape` 路線を踏襲した
小さめのピル形状（背景 `NothingGrays.Base`、文字は白、8〜10sp）。表示内容は符号付き数値
のみ（例: `-67`、単位表記なし）。電波が弱い時（目安: セルラー -110dBm以下 /
Wi-Fi -80dBm以下）は文字色をアクセント赤（`#D1432B`）に変える。

Wi-Fi RSSI取得には `ACCESS_WIFI_STATE`（通常権限、宣言のみで実行時プロンプト不要）と
`ACCESS_FINE_LOCATION`（現状 `ACCESS_COARSE_LOCATION` のみのため格上げが必要）を
新規に要求する。`MainActivity.kt` の既存パターン
（`registerForActivityResult(RequestPermission())` を `onCreate()` で都度 `launch()`、
理由説明UIなし）にそのまま乗せる。

検討した代替案:
- バッテリー温度バッジ → 却下（バッテリー%と情報が近く、目新しさが薄いため）
- 環境光センサー(lux) → 見送り（常時 `SensorManager` リスナーを張る必要があり実装コストが
  見合わないため。将来別機能として再検討の余地あり）
- 充電中の電圧/電流(mV/mA) → 見送り（非充電時に表示するものがなく、常時性のある
  バッジという方向性に合わないため）
- セルラーのみ対応してWi-Fi RSSIを諦める → 見送り（実利用時間の多くがWi-Fi接続中のため、
  バッジが機能しない時間が長くなりすぎる）

## 変更対象

- `AndroidManifest.xml`: `ACCESS_WIFI_STATE` を追加、`ACCESS_COARSE_LOCATION` を
  `ACCESS_FINE_LOCATION` に格上げ（既存の粗い位置情報用途があれば併用を確認）
- `MainActivity.kt`: 新規 `registerForActivityResult(RequestPermission())` を追加し
  `onCreate()` で `ACCESS_FINE_LOCATION` をリクエスト。`StatusIconsController` の
  新規 `signalDbm` Flowを `collectAsState()` して `HomeScreen.kt` に渡す
- `StatusIconsController.kt`: `SignalStrengthsListener` 内で生dBm値を取得し
  `_signalDbm: MutableStateFlow<Int?>` として公開。`WifiManager` を新規に使い
  Wi-Fi接続中のRSSIを優先的に読む
- `StatusMath.kt`（または同等の純粋関数ファイル）: dBm値→弱電波判定（赤にするか）の
  閾値ロジックを純粋関数として追加
- `RotaryKnob.kt`: `badgeContent: (@Composable () -> Unit)? = null` パラメータを追加し、
  `Canvas` のみを `Box` でラップして `Alignment.TopEnd` に描画
- 新規: `ui/SignalBadge.kt`（仮）: dBm値を受け取りピル形状のバッジを描画する
  Composable。閾値判定は `StatusMath.kt` 側の関数を利用
- `HomeScreen.kt`: `HOME_2` の `RotaryKnob` 呼び出しにのみ `badgeContent` を渡す

## テスト

dBm値→弱電波判定（赤色にするかどうか）の閾値ロジックは既存の `StatusMathTest` と
同様の形で純粋関数として切り出し、ユニットテストを追加する
（境界値: セルラー-110dBm/-109dBm、Wi-Fi-80dBm/-79dBm、`null`時は非表示扱い）。
権限未許可・SIMなし・Wi-Fi未接続などの組み合わせによるフォールバック挙動も
可能な範囲でユニットテスト化する。UI自体（バッジの位置・色の実描画）は既存の
ノブ関連コンポーネント同様、目視確認とする。
