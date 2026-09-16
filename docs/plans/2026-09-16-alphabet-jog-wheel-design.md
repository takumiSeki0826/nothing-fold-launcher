# 検索画面 左半分：アルファベット・ジョグホイール

## 背景

[[2026-09-16-app-drawer-split-layout-design]]でZ Fold 8展開時の検索画面（`AppDrawer.kt`）左半分に「お気に入りグリッド」を実装したが、実機で試した結果、DJ機材のジョグホイール風UIに差し替えたいという要望。機能は「回すとアルファベットインデックスをスクラブし、検索結果リストがその文字の位置までスクロールする」というもの。既存の`AlphabetIndexBar.kt`（縦バーをドラッグして文字を選ぶ）の円形版にあたる。

## スコープ

- `AppDrawer.kt`の`isExpandedWidth == true`時の左ペインを、お気に入り`AppGrid`から新規`AlphabetJogWheel`に置き換える
- お気に入り表示機能自体（`homeApps`/`iconColorFor`の検索画面への受け渡し）は削除する。ホーム画面側のお気に入り機能には影響しない
- 非展開時（compact）のレイアウトは変更しない

## アーキテクチャ / コンポーネント

- **`AlphabetIndex.kt`に純粋関数を追加**: `letterForWheelAngleDeg(angleDeg: Float): Char`
  - 0°=A、時計回りに27分割（A〜Z + `#`）で一周。既存`letterForBarPosition(relativeY: Float): Char`の円形版
  - 角度は`RotaryKnobMath.kt`の既存規則（0°=真上、時計回りが正）に合わせる
- **新規`AlphabetJogWheel.kt`**: `AlphabetJogWheel` Composable
  - 見た目は`RotaryKnob.kt`と同系統の配色（`KNOB_FILL_COLOR`/`KNOB_OUTLINE_COLOR`等）を流用した円形キャンバス
  - ドラッグジェスチャーは`AlphabetIndexBar.kt`の`pointerInput { awaitEachGesture { ... } }`パターンを踏襲。指位置→中心からの相対座標→角度→文字に変換
  - 文字が変わるたびに`onLetterSelected(letter)`を呼び、`HapticFeedbackType.SegmentTick`でハプティック
  - 中心にテキストで現在の文字を表示（ドラッグ中は大きく、アイドル時は薄く最後の文字。初期値`#`）
  - タップ単体（ドラッグなし）では何も起きない
- **タッチ座標→角度の純粋関数**（Composable本体から切り出し、`RotaryKnobMathTest.kt`と同様にテスト可能な形にする）: 中心からの相対座標`(dx, dy)`を受け取り、0°=真上・時計回り正の角度を返す
- **`AppDrawer.kt`**: 左ペインの`AppGrid`呼び出しを`AlphabetJogWheel(onLetterSelected = { letter -> scrollIndexForLetter(letter, letterIndexMap, apps.size)?.let { coroutineScope.launch { listState.scrollToItem(it) } } })`に置き換え。この処理は既存の`AlphabetIndexBar`の`onLetterSelected`ラムダと同一ロジックなので、共通化するかコピーするかは実装時に判断
- **`AppDrawer.kt`/`MainActivity.kt`**: 未使用になる`homeApps`・`iconColorFor`パラメータと配線を削除

## エラーハンドリング / エッジケース

- アプリ0件・少数件でも`scrollIndexForLetter`が`null`を返す既存の安全策でクラッシュしない（`?.let`で無視）
- ドラッグ位置が中心に極端に近くても、角度計算は半径に依存しない（`atan2`ベース）ため特別扱い不要
- 右側のアルファベットインデックスバーが20件未満で非表示（`shouldShowAlphabetIndex`）でも、左のジョグホイールは件数に関係なく常時表示・機能する

## テスト方針（TDD）

- `letterForWheelAngleDeg`と座標→角度変換関数は、`AlphabetIndexTest.kt`（および必要なら`RotaryKnobMathTest.kt`同様の新規テストファイル）にTDDで追加する
- ドラッグジェスチャー自体（Composableの`pointerInput`部分）は、既存の`AlphabetIndexBar`と同じくユニットテスト対象外とし、実機で目視・操作確認する
