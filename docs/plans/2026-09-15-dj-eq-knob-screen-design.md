# DJ機器風EQノブスタック画面（ホーム左スワイプ）

## 背景

ホーム画面から左スワイプで表示できる新しい画面を追加し、中央にDJ機器デザインのEQノブスタック（左右対称2チャンネル×MID/LOW/CFXの3ノブ）を実装したいという要望。今回はノブの見た目とドラッグ操作のみを実装し、実際の音量・EQ制御やアプリ起動機能（将来対応予定）とは連動させない。

## スコープ

- 新規画面（以下「EQ画面」）を追加し、ホーム画面から左スワイプで表示、右スワイプでホームに戻る
- EQ画面の中身は仕様書通り「ノブスタックのみ」。時計・ステータスアイコン等は配置しない
- ノブの値は見た目上ドラッグで変化するが、システムの音量・EQには一切影響しない。値はComposeの`remember`でのみ保持し、プロセス終了・画面破棄でリセットされてよい（永続化不要）
- ノブをタップしてアプリを起動する機能は将来の別対応とし、本設計には含めない

## 画面遷移・アーキテクチャ（`MainActivity.kt`）

現状、`MainActivity.kt`は`showDrawerState: Boolean`の単一状態と、最上位`Box`の`Modifier.pointerInput(showDrawer) { detectDragGestures(...) }`でドロワーの開閉ジェスチャーを処理し、`AnimatedContent`で縦スライド遷移させている。

- `Boolean`の状態を`enum class HomeRoute { HOME, DRAWER, EQ }`に置き換える
- ジェスチャー判定を拡張する:
  - `HOME`表示中に左スワイプ（`dragAccumX < -120f`）→ `EQ`へ
  - `EQ`表示中に右スワイプ（`dragAccumX > 120f`）→ `HOME`へ
  - 既存の「上スワイプ→`DRAWER`」「下/左スワイプ→`HOME`（ドロワーから）」はそのまま維持
- `AnimatedContent`の`transitionSpec`ラムダで`initialState`/`targetState`の組み合わせを見て、Home⇔Drawerは縦スライド（既存通り）、Home⇔EQは横スライド（`slideInHorizontally`/`slideOutHorizontally`）を出し分ける
- ハードウェア戻るボタンは現状ドロワーにも未実装（`BackHandler`なし）のため、EQ画面も同様にスコープ外とする（スワイプのみで遷移）

## RotaryKnobコンポーネント

新規ファイル`ui/RotaryKnobMath.kt`（純粋関数、テスト対象）と`ui/RotaryKnob.kt`（Composable、`FaderSlider.kt`と同じ構成パターン）を追加する。

### RotaryKnobMath.kt

```kotlin
fun dragDeltaToAngle(currentAngleDeg: Float, dragDeltaYPx: Float, sensitivity: Float): Float =
    (currentAngleDeg - dragDeltaYPx * sensitivity).coerceIn(-135f, 135f)

fun angleToIndicatorOffset(angleDeg: Float, radiusPx: Float): Offset {
    val radians = Math.toRadians((angleDeg - 90.0))
    return Offset(radiusPx * cos(radians).toFloat(), radiusPx * sin(radians).toFloat())
}
```

- `dragDeltaToAngle`: 上方向ドラッグ（負のdeltaY）で角度を増加（時計回り＝値を上げる)、下方向で減少。`-135f..135f`（12時を中心に270度）にクランプ
- `angleToIndicatorOffset`: 12時方向を0度とした角度から、Canvas描画用のオフセット（`-90`補正して標準的な`cos/sin`角度系に変換）を返す

### RotaryKnob.kt

- シグネチャ: `RotaryKnob(angleDeg: Float, onAngleChange: (Float) -> Unit, label: String, modifier: Modifier = Modifier)`（`FaderSlider`同様、状態ホイスティング型）
- 直径60dp、`Canvas` + `Modifier.pointerInput(Unit) { detectVerticalDragGestures { change, dragAmount -> onAngleChange(dragDeltaToAngle(angleDeg, dragAmount, SENSITIVITY)) } }`
- 描画順: ノブ本体円（`#333333`塗り）→ 白い回転インジケーター線（中心から`angleToIndicatorOffset`方向へ）→ 中心白ドット → 下部にラベルText（8-10sp、白、「MID」「LOW」「CFX」）
- 淡いグロー効果: `hoverable`による`isHovered`（マウス/スタイラス向け）と、ドラッグ中フラグ（タッチ向けのフィードバック代替）のいずれかが真の間、ノブ円の外側に半透明白の`blur`付き円を重ねて表現
- 初期値: `angleDeg = 0f`（12時＝中央）
- ドラッグ感度（`SENSITIVITY`）の具体値は実装時に決定する

## EqScreen.kt（画面レイアウト・状態管理）

- ルート: `Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D)))))`
- 中央に`Row(Modifier.align(Alignment.Center))`を配置し、左右チャンネルを対称配置
  - 各チャンネル = `Column`に`RotaryKnob(label = "MID")` → `RotaryKnob(label = "LOW")` → `RotaryKnob(label = "CFX")`を縦並び（上から下）
  - 左右の`Column`間のスペーシングと、各`Column`内部のスペーシングは共通の定数を使い、左右完全対称にする
- 状態: 6ノブ分の角度を`EqScreen`内で個別に`var leftMid by remember { mutableStateOf(0f) }` … のように保持する（`rememberSaveable`は使わず、永続化しない）
- 他の要素（時計・ステータスアイコン等）は配置しない

## テスト方針

- `RotaryKnobMath.kt`の`dragDeltaToAngle`・`angleToIndicatorOffset`は純粋関数のため、`HomeLayoutMathTest.kt`と同様の単体テスト（`RotaryKnobMathTest.kt`）を追加する
  - `dragDeltaToAngle`: 範囲内での増減の向き、`-135f`/`135f`での境界クランプ
  - `angleToIndicatorOffset`: `0f`（12時＝真上）、`-135f`、`135f`など代表角度でのオフセット値
- 画面遷移のスワイプジェスチャーとノブの見た目・グロー効果は、既存の設計docと同様に実機目視確認とする
