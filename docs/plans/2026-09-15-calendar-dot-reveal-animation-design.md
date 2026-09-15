# カレンダードットのランダムスパークルアニメーション

## 背景

ホーム画面のカレンダーウィジェット（[[2026-09-14-slider-bounce-entrance-animation-design.md]]で実装したスライダーのバウンド登場アニメーションと同じく、ホーム画面が表示されるたびに演出を加えたい）のドットマトリクスに、オシャレなランダム点滅の登場アニメーションを追加したいという要望。

当初「ドットが非表示状態からランダムにフェードインする」案で実装したが、要望の実際のイメージは「ドット自体は常に表示されたまま、ランダムなドットが一瞬アクセントカラーに光ってグレーに戻る」というスパークル演出だったため、以下の内容に差し替えた。

## スコープ

- `CalendarWidget.kt`内の`MiniDotCalendar`にのみ適用（`HomeScreen.kt`のコンパクト・拡張レイアウト両方から共有される）
- ロック画面ウォールペーパー側のカレンダー描画（`NothingWallpaperRenderer.kt`）には適用しない
- 発火タイミング: ホーム画面が表示されるたび毎回（アプリ起動時、およびドロワーを閉じてホームに戻るたび）
- 本日を示す赤いドットはアニメーション対象外。最初から常時アクセントカラーで固定表示のままにする（目印としての視認性を優先）
- ドット自体の表示・非表示（不透明度）は変化させない。変化するのは色のみ

## 実装

### 純粋ロジック

`CalendarDotGrid.kt`に以下の純粋関数を追加する:

```kotlin
fun dotFlashIntensity(progress: Float, peak: Float, pulseWidth: Float): Float =
    (1f - abs(progress - peak) / pulseWidth).coerceIn(0f, 1f)
```

- `progress`: アニメーション全体の進行度（0f→1f）
- `peak`: そのドットの光がもっとも強くなる進行度（ドットごとにランダム、`pulseWidth`〜`1 - pulseWidth`の範囲に収める）
- `pulseWidth`: `peak`を中心に立ち上がり・立ち下がりにかかる進行度の幅（0.15f固定）

三角波状のパルスで、`progress`が`peak`に達した瞬間に`intensity = 1`（完全にアクセントカラー）、そこから離れるほど`0`（通常のグレー）に近づく。`peak`をドットごとにランダムに散らすだけで、ドット数分のコルーチンを立てずに1つの共有進行度から「バラバラなタイミングで一瞬光ってグレーに戻る」スパークルを表現できる。

### Compose側

`MiniDotCalendar`に以下を追加:

- `val flashPeaks = remember(grid) { grid.dots.associate { it.day to (PULSE_WIDTH + Random.nextFloat() * (1f - 2 * PULSE_WIDTH)) } }`
  - `grid`（月やドット数）が変わるたびに再生成。`peak`を`[pulseWidth, 1 - pulseWidth]`に収めることで、アニメーション区間内で立ち上がり・立ち下がりが必ず完結する
- `val flashProgress = remember(grid) { Animatable(0f) }`
- `LaunchedEffect(grid) { flashProgress.animateTo(1f, tween(durationMillis = 500, easing = LinearEasing)) }`
  - `SlidersRow`と同様、このコンポーザブルは`MainActivity`の`AnimatedContent`がdrawer⇔home間で切り替わるたびに新規コンポジションとして生成されるため、`remember`状態がリセットされ「毎回」発火する
- `Canvas`の`drawCircle`呼び出しで、本日ドット以外は`androidx.compose.ui.graphics.lerp(グレー, アクセントカラー, dotFlashIntensity(...))`で色をブレンドする。本日ドットは常にアクセントカラー固定

### 定数

- `DOT_FLASH_PULSE_WIDTH = 0.15f`（各ドット個別の立ち上がり・立ち下がり速度）
- アニメーション全体の長さ: 500ms、`LinearEasing`（`progress`自体を線形に進め、ランダム性は`peak`の散らばりだけで表現するため）

## テスト方針

- `dotFlashIntensity`は純粋な数式なので、`HomeLayoutMathTest.kt`と同様の単体テストを追加する
  - `peak`から`pulseWidth`以上離れている → `0f`
  - `progress == peak` → `1f`
  - `peak`の前後`pulseWidth/2`の地点 → `0.5f`（線形の立ち上がり・立ち下がり）
  - `peak ± pulseWidth`のちょうど境界 → `0f`
- Compose側の見た目（色の変化タイミング・ランダム性）は、既存の設計docと同様に実機目視確認とする
