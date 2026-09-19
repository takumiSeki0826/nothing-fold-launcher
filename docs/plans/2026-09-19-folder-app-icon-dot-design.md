# フォルダ内アプリアイコンへのオレンジドット追加

## 背景

ホーム画面のアプリアイコン（`HomeScreen.kt` の `AppIconTile`）には右上にオレンジドット
（`#D1432B`、8dp、表示のたびにランダムな距離で落下→2段バウンド→着地するアニメーション）が
付いているが、フォルダを開いたときに表示されるアプリ一覧（`FolderOverlay.kt` の
`FolderOverlayAppTile`）には付いていない。フォルダを開いたときのアプリにも同じドットを
表示してほしいという要望。

## 方針

ドット描画とアニメーションのロジック（定数・easing・4段階の `animateTo`）を
`HomeScreen.kt` から新規ファイル `ui/AppIconDot.kt` の公開コンポーザブル `AppIconDot()` に
切り出し、`AppIconTile` と `FolderOverlayAppTile` の両方から利用する。見た目・挙動は
ホーム画面と完全に同一（サイズ・色・アニメーションカーブとも変更なし）。

検討した代替案:
- `FolderOverlayAppTile` にロジックをコピペする → アニメーション定数やeasingが2箇所に
  重複し、将来の調整で片方だけ直し忘れるリスクがあるため不採用。
- `HomeScreen.kt` 側の関数を `private` から `internal`/`public` にして直接呼ぶ → ドットの
  責務がホーム画面のファイルに残ったままになり、フォルダ側からの依存として不自然なため不採用。

## 変更対象

- 新規: `ui/AppIconDot.kt`
  - `AppIconDot(modifier: Modifier = Modifier)`: ドット本体+落下バウンドアニメーション
  - 関連定数（`APP_ICON_DOT_COLOR`, `APP_ICON_DOT_SIZE`, `APP_ICON_DOT_INSET`,
    落下距離・バウンド・duration・easing定数）と `scaledDurationMs` をここに移動
- `HomeScreen.kt`: `AppIconTile` 内のドット描画部分を `AppIconDot()` 呼び出しに置き換え、
  移動した定数・関数を削除
- `FolderOverlay.kt`: `FolderOverlayAppTile` のアイコン `Box` に `AppIconDot()` を
  `Alignment.TopEnd` で重ねて配置

## テスト

既存のUIであり、アニメーション計算ロジック自体（`scaledDurationMs` の丸め等）は
`DotMath`/`CalendarDotGrid` 同様に将来ユニットテスト化できるが、今回は既存の
`AppIconTile` から関数を移動するだけで振る舞いを変えないため、新規テストは追加しない。
