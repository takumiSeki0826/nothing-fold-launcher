# EQ画面ノブの入場ウォブルアニメーション

## 背景

ホーム画面から左スワイプでEQ画面（[[2026-09-15-dj-eq-knob-screen-design.md]]）に遷移した際、ノブのオレンジのドットが少しだけ回転して揺れるような演出を加えたいという要望。

## スコープ

- EQ画面が表示されるたび毎回（ホーム→EQへのスワイプに限らず、EQ画面のコンポジションが新規生成されるタイミングすべて）、6つのノブのオレンジドットが少しだけ回転して揺れる
- 6つのノブは同時ではなく、描画順（左チャンネル上から下→右チャンネル上から下）にごく短い間隔でずれて順に揺れる
- 揺れの質感は「一方向に少し回転し、springで一瞬オーバーシュートしながら元の角度に自然に戻る」タイプ（左右に何度も振れるシェイクではない）
- 既存の「タップ時にドットが270度回転してspringで戻る」アニメーション（`RotaryKnob.kt`の`pressRotationDeg`）はそのまま維持し、今回追加する入場ウォブルと同時に発生しても自然に合成されるようにする

## 実装

### 発火の仕組み

`MainActivity.kt`の`AnimatedContent(targetState = homeRoute)`は`route`ごとにコンテンツラムダを呼び直すため、`HomeRoute.EQ`になるたびに`EqScreen`以下は新規コンポジションとして生成される（[[2026-09-15-calendar-dot-reveal-animation-design.md]]のカレンダードットスパークルと同じ仕組み）。これを利用し、`RotaryKnob`内の`LaunchedEffect(Unit)`で発火させることで、追加の状態管理なしで「EQ画面表示のたび毎回」再生される。

### RotaryKnob.kt

- 新規状態`entranceWobbleDeg = remember { Animatable(0f) }`を追加
- 新規パラメータ`entranceWobbleDelayMs: Long = 0L`を追加（ノブごとのずらし用）
- `LaunchedEffect(Unit)`で以下を実行:
  1. `delay(entranceWobbleDelayMs)`
  2. `entranceWobbleDeg.animateTo(ENTRANCE_WOBBLE_DEG, spring(...))`でわずかに回転
  3. `entranceWobbleDeg.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))`で元に戻す（既存の`pressRotationDeg`の release アニメーションと同じdampingRatioを使い、一瞬のオーバーシュートで自然に止まる質感を揃える）
- ドットの描画位置計算を次のように変更:
  - 変更前: `angleToIndicatorOffset(angleDeg + pressRotationDeg.value, ...)`
  - 変更後: `angleToIndicatorOffset(angleDeg + pressRotationDeg.value + entranceWobbleDeg.value, ...)`
- 定数: `ENTRANCE_WOBBLE_DEG = 14f`（プレス回転の270度に対してごく小さい値。実機確認で調整）

### EqScreen.kt

- `EqChannel`内の`for (slot in slots)`ループを`slots.forEachIndexed`に変更し、左チャンネル（index 0,1,2）→右チャンネル（index 3,4,5）の通し番号でディレイを計算
  - 左チャンネルは`index * STAGGER_DELAY_MS`、右チャンネルは`(index + slots.size) * STAGGER_DELAY_MS`を`RotaryKnob`の`entranceWobbleDelayMs`に渡す
- 定数: `STAGGER_DELAY_MS = 40L`（6ノブ合計で200msのずれ幅。実機確認で調整）

## テスト方針

角度計算のコア関数`angleToIndicatorOffset`は既存の単体テストで担保済みで、今回の変更はそれに加算する値と発火タイミングのみ。springアニメーションのタイミング制御は純粋ロジックを含まないため、既存の押下アニメーション等と同様に単体テスト対象外とし、実機目視確認とする。
