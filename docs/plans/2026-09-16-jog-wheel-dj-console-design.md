# ジョグホイールのDJ機材風ビジュアル刷新

## 背景

[[2026-09-16-alphabet-jog-wheel-design]]で実装した`AlphabetJogWheel`は単色の円＋中央の文字のみとシンプルすぎる。ユーザーが提示した実機DJミキサーのジョグホイール画像を参考に、ブラッシュドメタル調の見た目・タッチ位置が光る演出・周囲のDJ機材っぽいボタンを追加し、「DJできそうな感じ」に寄せる。

## スコープ

`AlphabetJogWheel.kt`の見た目とインタラクションを拡張する。`AppDrawer.kt`は新規コールバック（検索クリア・先頭/末尾ジャンプ）の配線のみ追加する。既存の文字スクラブ機能（`onLetterSelected`）の呼び出し契約は変更しない。

## ホイール本体の見た目

- 中心から外周に向けて複数の同心円グラデーション（明暗交互）を重ね、金属の質感を疑似的に表現
- 中央に斜め十字（X字）のハイライトストロークを描画（参考画像のブラッシュドメタルの反射感）
- 外周に太めの暗いベゼルリングを追加
- 基調は既存`KNOB_FILL_COLOR`系のダークグレー。アクセントカラーは触っている時のみ使用

## タッチ中の光る演出

- ドラッグ中、現在の角度を中心に外周リング上に約30〜40°幅のオレンジ色（`Color(0xFFD1432B)`、アプリ既存アクセントカラー。お気に入り☆や既存`RotaryKnob`のドットと同一）のアークを描画。中心が最も明るく両端はフェードアウト
- 指を離すとアークは消える（アイドル時はリングは暗いまま）
- 中央の文字表示（大きく現在の文字、アイドル時は薄く最後の文字）は現状の演出を維持

## 周囲のボタン（4つ、既存RotaryKnobを再利用）

新規UIは作らず、既存の`RotaryKnob`コンポーネントを小さめのdiameterでそのまま流用し、ホイールの四隅に配置する。

| 位置 | ラベル | 機能 |
|---|---|---|
| 左上 | CLR | 検索テキストをクリア（`onQueryChange("")`） |
| 左下 | TOP | 検索結果リストの先頭へジャンプ（`listState.scrollToItem(0)`） |
| 左下（TOPの下） | END | 検索結果リストの末尾へジャンプ（`listState.scrollToItem(apps.lastIndex)`） |
| 右下 | REV | ホイールの回転方向（A→Z / Z→A）をトグル |

- CLR/TOP/ENDは`angleDeg = 0f`固定
- REVのみ、内部状態`reversed`に応じて`angleDeg`を0°/180°に切り替え、トグル状態を視覚的に示す

## アーキテクチャ / データフロー

- `AlphabetJogWheel`に新規パラメータを追加: `onClearSearch: () -> Unit`、`onJumpToStart: () -> Unit`、`onJumpToEnd: () -> Unit`
- 回転方向トグル`reversed: Boolean`はホイール内部の`remember`状態として保持（外部に公開しない）
- 既存の`letterForWheelAngleDeg(angleDeg: Float): Char`に`reversed: Boolean = false`パラメータを追加（デフォルト値のため既存呼び出し・既存テストは無変更のまま通る）。`reversed = true`のときは角度を`360f - angleDeg`に反転して渡すことで、回す方向とアルファベット進行を逆にする
- タッチ発光アークの角度計算（Composeの`drawArc`は0°=3時方向・時計回りが正、既存の`angleDeg`規則は0°=真上・時計回りが正）を変換する純粋関数を追加
- `AppDrawer.kt`側で新規コールバックを接続:
  - `onClearSearch = { onQueryChange("") }`
  - `onJumpToStart = { coroutineScope.launch { listState.scrollToItem(0) } }`
  - `onJumpToEnd = { coroutineScope.launch { listState.scrollToItem((apps.size - 1).coerceAtLeast(0)) } }`

## エラーハンドリング / エッジケース

- `apps`が空のとき、END用の`scrollToItem((apps.size - 1).coerceAtLeast(0))`は`scrollToItem(0)`相当になり、`LazyColumn`側も0件表示のため実害なし
- `reversed`状態はホイール内部のUI状態のみで、検索画面を閉じて開き直すとリセットされる（永続化しない）。これは意図的な仕様

## テスト方針（TDD）

- `letterForWheelAngleDeg`の`reversed = true`時の挙動を`AlphabetIndexTest.kt`に追加テスト
- タッチ発光アークの角度変換（`angleDeg`規則→Composeの`drawArc`規則）を純粋関数として切り出し、`RotaryKnobMathTest.kt`と同様のスタイルでテスト
- Canvasの見た目（グラデーション・十字ハイライト・ベゼル）とボタン配置は既存方針通りユニットテスト対象外とし、実機で確認する
