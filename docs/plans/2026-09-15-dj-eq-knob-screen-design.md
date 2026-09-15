# DJ機器風EQノブスタック画面（ホーム左スワイプ）

## 背景

ホーム画面から左スワイプで表示できる新しい画面を追加し、中央にDJ機器デザインのEQノブスタック（左右対称2チャンネル×MID/LOW/CFXの3ノブ）を実装したいという要望。

初版ではノブの見た目とドラッグ操作のみを実装したが、実機で試した結果ドラッグ操作が直感的でなかったため、タップ/長押しで直接アプリを起動するランチャーに変更し、あわせて見た目もよりリアルな（グロッシーなドーム型・ローレット加工の）ノブデザインに刷新する（[改訂](#改訂-アプリランチャー化と見た目刷新)を参照）。

## スコープ（初版）

- 新規画面（以下「EQ画面」）を追加し、ホーム画面から左スワイプで表示、右スワイプでホームに戻る
- EQ画面の中身は仕様書通り「ノブスタックのみ」。時計・ステータスアイコン等は配置しない
- ノブをタップしてアプリを起動する機能は将来の別対応とし、本設計には含めない（→ 改訂で対応）

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

## テスト方針（初版）

- `RotaryKnobMath.kt`の`dragDeltaToAngle`・`angleToIndicatorOffset`は純粋関数のため、`HomeLayoutMathTest.kt`と同様の単体テスト（`RotaryKnobMathTest.kt`）を追加する
- 画面遷移のスワイプジェスチャーとノブの見た目・グロー効果は、既存の設計docと同様に実機目視確認とする

## 改訂: アプリランチャー化と見た目刷新

初版を実機で確認したところ、ドラッグでの回転操作が直感的でなかったため、以下の通り変更する。

### 変更点の要約

1. ノブの直径を60dp→150dp（2.5倍）に拡大
2. 操作方式をドラッグ回転から**タップ/長押し**に変更し、タップで「アプリ1」・長押しで「アプリ2」を起動するランチャーにする
3. 見た目を、グロッシーなドーム型・側面ローレット加工のリアルな黒いノブ風デザインに刷新する。オレンジ（`0xFFD1432B`、既存の`NothingRed`）の固定ドットを、ノブごとに異なる固定角度（本物のEQノブスタックのように見える演出。回転操作はしない）で配置する
4. ノブのドラッグによる値変更機能・その永続化不要方針は撤回し、代わりにタップ/長押しに割り当てたアプリの組み合わせを永続化する

### データモデル・永続化

- **`ui/KnobSlot.kt`（新規）**: 6つのノブ位置を表す`enum class KnobSlot(val id: String, val decorativeAngleDeg: Float)`
  - `LEFT_MID("left_mid", 15f)`, `LEFT_LOW("left_low", -60f)`, `LEFT_CFX("left_cfx", 90f)`
  - `RIGHT_MID("right_mid", -15f)`, `RIGHT_LOW("right_low", 60f)`, `RIGHT_CFX("right_cfx", -90f)`
- **`data/EqKnobAssignmentStore.kt`（新規）**: `FavoritesStore.kt`と同じSharedPreferencesパターン。`"${slot.id}_tap"` / `"${slot.id}_long"`をキーにパッケージ名（String）を保存・取得する（12スロット分）

### アプリ選択フロー（`MainActivity.kt`）

- `pendingKnobPick: Pair<KnobSlot, Boolean>?`（`Boolean`は長押しかどうか）をComposeの`remember`状態として追加
- 未設定スロットをタップ/長押し → `pendingKnobPick`をセットし`homeRoute = HomeRoute.DRAWER`でアプリ一覧を開く
- ドロワーでアプリを選択 → 選んだパッケージ名を該当スロット・アクションに保存し、`pendingKnobPick = null`、`homeRoute = HomeRoute.EQ`に戻す（`HomeRoute.HOME`には戻さない）
- ドロワーをスワイプで閉じてキャンセルした場合も、`pendingKnobPick`が立っていれば`HomeRoute.EQ`に、立っていなければ従来通り`HomeRoute.HOME`に戻す
- 設定済みスロットをタップ/長押し → 通常通り既存の`launchApp(packageName)`を呼ぶ

### RotaryKnob.kt（全面書き換え）

- 直径150dp
- 描画（`Canvas`に複数の`drawCircle`/`drawArc`を重ねる）:
  - ベース円: 中心をやや左上にオフセットした`Brush.radialGradient`でハイライト→暗部のグラデーションを作り、ドーム型の立体感を表現
  - ローレット加工: 円周付近に短い放射状の線を等間隔（60本程度）に描画し、側面のギザギザ質感を表現
  - 光沢ハイライト: 左上寄りに半透明白の`radialGradient`を重ねて艶を追加
  - 固定ドット: 既存の`angleToIndicatorOffset`（`RotaryKnobMath.kt`に残す）で`slot.decorativeAngleDeg`の位置に`Color(0xFFD1432B)`の小さいドットを描画（回転操作はしない、演出のみ）
- 操作: `Modifier.combinedClickable(interactionSource = interactionSource, indication = null, onClick = onTap, onLongClick = onLongPress)`。`AppDrawer.kt`の`AppRow`・`HomeScreen.kt`の`AppIconTile`と同じ方式に合わせる
- `interactionSource.collectIsPressedAsState()`（＋`hoverable`の`isHovered`）で押下中に淡い白グローを表示
- ラベル: タップ用アプリが未設定なら`"+"`、設定済みならそのアプリ名（`app.label`）のテキストを8-10spで表示（実アイコン描画は行わない。既存の`AppIconTile`と同じくテキストベースのスタイルに揃える）
- `dragDeltaToAngle`とドラッグ関連コードは削除する

### EqScreen.kt（書き換え）

- 角度やアプリ割当を内部で`remember`する必要がなくなり、外部から渡されたデータを表示するだけの「dumb」なComposableになる
- シグネチャ: `EqScreen(assignments: Map<KnobSlot, KnobAppAssignment>, onKnobTap: (KnobSlot) -> Unit, onKnobLongPress: (KnobSlot) -> Unit, modifier: Modifier = Modifier)`
  - `KnobAppAssignment(val tapApp: AppInfo?, val longPressApp: AppInfo?)`
  - `onKnobTap`/`onKnobLongPress`は「そのスロットが押された」ことだけを通知し、未設定なのでピッカーを開くか、設定済みなので起動するかの判断は`MainActivity`側で行う
- レイアウト（左右対称2チャンネル×3ノブ縦並び）は初版のまま。ノブサイズ拡大に伴い、収まらない場合はスペーシングを調整する（実機確認で調整）

### テスト方針（改訂差分）

- `RotaryKnobMath.kt`: `dragDeltaToAngle`と対応テストは削除。`angleToIndicatorOffset`とそのテストは流用する
- 新規の`KnobSlot`・`EqKnobAssignmentStore`・アプリ選択フローの分岐（未設定→ピッカー、設定済み→起動）は、いずれも単純なデータ保持/受け渡しであり複雑な純粋ロジックを含まないため、既存の`FavoritesStore`等と同様に単体テスト対象外とし、実機目視確認とする
- 見た目（グロッシーな質感・ローレット・グロー）は実機目視確認とする
