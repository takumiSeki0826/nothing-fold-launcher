# カレンダードットのランダム点滅登場アニメーション

## 背景

ホーム画面のカレンダーウィジェット（[[2026-09-14-slider-bounce-entrance-animation-design.md]]で実装したスライダーのバウンド登場アニメーションと同じく、ホーム画面が表示されるたびに演出を加えたい）のドットマトリクスに、オシャレなランダム点滅の登場アニメーションを追加したいという要望。

## スコープ

- `CalendarWidget.kt`内の`MiniDotCalendar`にのみ適用（`HomeScreen.kt`のコンパクト・拡張レイアウト両方から共有される）
- ロック画面ウォールペーパー側のカレンダー描画（`NothingWallpaperRenderer.kt`）には適用しない
- 発火タイミング: ホーム画面が表示されるたび毎回（アプリ起動時、およびドロワーを閉じてホームに戻るたび）
- 本日を示す赤いドットはアニメーション対象外。最初から固定表示のままにする（目印としての視認性を優先）

## 実装

### 純粋ロジック

`CalendarDotGrid.kt`に以下の純粋関数を追加する:

```kotlin
fun dotRevealAlpha(progress: Float, threshold: Float, fadeWidth: Float): Float =
    ((progress - threshold) / fadeWidth).coerceIn(0f, 1f)
```

- `progress`: アニメーション全体の進行度（0f→1f）
- `threshold`: そのドットがフェードインを開始する進行度（ドットごとにランダム）
- `fadeWidth`: 1つのドットがフェードインしきるまでの進行度の幅（0.15f固定）

この関数により、`threshold`をドットごとにランダムに散らすだけで「バラバラなタイミングでフェードインしつつ、`progress`が1に達した時点で全ドットが必ず完全に点灯している」状態を、ドット数分のコルーチンを立てずに1つの進行度だけで表現できる。

### Compose側

`MiniDotCalendar`に以下を追加:

- `val thresholds = remember(grid) { List(grid.dots.size) { Random.nextFloat() * (1f - FADE_WIDTH) } }`
  - `grid`（月やドット数）が変わるたびに再生成
- `val progress = remember { Animatable(0f) }`
- `LaunchedEffect(Unit) { progress.animateTo(1f, tween(durationMillis = 500, easing = LinearEasing)) }`
  - `SlidersRow`と同様、このコンポーザブルは`MainActivity`の`AnimatedContent`がdrawer⇔home間で切り替わるたびに新規コンポジションとして生成されるため、`remember`状態がリセットされ「毎回」発火する
- `Canvas`の`drawCircle`呼び出しで、本日ドット以外は`color.copy(alpha = dotRevealAlpha(progress.value, thresholds[index], FADE_WIDTH))`を使う。本日ドットは常に`alpha = 1f`

### 定数

- `FADE_WIDTH = 0.15f`（各ドット個別のフェードイン速度）
- アニメーション全体の長さ: 500ms、`LinearEasing`（`progress`自体を線形に進め、ランダム性はしきい値の散らばりだけで表現するため）

## テスト方針

- `dotRevealAlpha`は純粋な数式なので、`HomeLayoutMathTest.kt`と同様の単体テストを追加する
  - `progress = 0f` → 常に`0f`
  - `progress >= threshold + fadeWidth` → `1f`
  - `progress`が`threshold`と`threshold + fadeWidth`の間 → `0f`〜`1f`の線形補間
  - `threshold`が範囲外（負値など）の場合の境界も確認
- Compose側の見た目（フェードインのタイミング・ランダム性）は、既存の設計docと同様に実機目視確認とする
