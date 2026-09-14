# Audio/Brightスライダーのバウンド登場アニメーション

## 背景

ホーム画面（[[2026-09-14-fold-expanded-screen-layout-design.md]]）のAudio/Brightスライダーに、表示のたびに軽くバウンドする登場アニメーションを追加したいという要望。

## スコープ

- `HomeScreen.kt`内の共通`SlidersRow`（コンパクト・展開の両レイアウトで共有）にのみ適用
- 発火タイミング: ホーム画面が表示されるたび毎回（アプリ起動時、およびドロワーを閉じてホームに戻るたび）

## 実装

- `SlidersRow`内で`remember { Animatable(60f) }`（初期値: 60dp下にオフセット）を保持
- `LaunchedEffect(Unit)`で`0f`まで`spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)`でアニメーション
- `Modifier.offset { IntOffset(0, value.roundToInt()) }`をRowに適用してtranslationYとして反映
- `SlidersRow`は`MainActivity`の`AnimatedContent`がdrawer⇔home間で切り替わるたびに新しいコンポジションとして生成される（`remember`状態がリセットされる）ため、追加の状態管理なしで「毎回」発火する

## テスト方針

- アニメーションのパラメータ（spring定数・初期オフセット量）は見た目の調整であり、切り出せる純粋ロジックがないため、既存の設計docと同様に実機目視確認とする
