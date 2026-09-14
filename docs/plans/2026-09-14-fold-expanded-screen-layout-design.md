# Z Fold 8 展開時ホーム画面レイアウト再設計

## 背景

`isExpandedWidth`分岐（[[2026-09-14-longpress-swipehome-fold-design.md]]で導入）は、左カラムに時計・カレンダー・Now Playing・スライダー類を縦に詰めて配置し、右カラムをアプリグリッド専用にする構成だった。実機で確認した結果、展開時の広い画面をもっと画面全体を使った左右2ペイン構成にしたいという要望。

## スコープ

`HomeScreen.kt`の`isExpandedWidth == true`分岐のみを書き換える。compact（折りたたみ時）のレイアウトは変更しない。

## レイアウト

```
┌─────────────────┬───────────────────────────┐
│                  │   Volume ┃ Bright          │ ← 右上 1/3
│   Clock + Status │  (SlidersRow中央寄せ)       │
├──────────────────┤───────────────────────────┤
│                  │                             │
│  Calendar        │                             │
│                  │   App Grid (4列×3行 固定)    │ ← 右下 2/3
├──────────────────┤                             │
│                  │                             │
│  Now Playing     │                             │
└──────────────────┴───────────────────────────┘
      左側 (縦3分割)              右側 (縦2分割)
```

- 左右比率: 既存の`weight(4f)` / `weight(6f)`を踏襲
- **左カラム**: `Column`を`weight(1f)`の`Box`3つで均等分割
  1. `ClockRow`（時計＋バッテリー/Wi-Fi/電波のステータスアイコン、既存コンポーネントそのまま）
  2. `CalendarWidget`（既存の黒い角丸カード、日付ドットカレンダー）
  3. `NowPlayingWidget`（既存の黒い角丸カード、曲名＋再生/一時停止）
  - 各ウィジェットは内部構造を変更せず、`Modifier.fillMaxWidth()`で幅いっぱいに広げて配置するのみ
- **右カラム**: `Column`を`weight(1f)`（スライダー）と`weight(2f)`（アプリグリッド）で分割
  - 上1/3: 既存の`SlidersRow`（`VolumeSlider` + `FaderSlider("Bright")`）を`Box(contentAlignment = Alignment.Center)`で中央配置
  - 下2/3: `AppGrid`を列数固定4、`apps.take(12)`で最大12個（4列×3行）に制限。超過分は非表示（スクロールは実装しない）
- `errorMessage`（アプリ一覧取得エラー）は左カラムのNow Playingカードの下に小さく表示（従来同様、レイアウトの主眼ではない）

## 変更ファイル

- `HomeScreen.kt`: `isExpandedWidth == true`分岐を上記構成に書き換え。compact分岐・`ClockRow`/`SlidersRow`/`AppGrid`などのprivate composableは可能な限り再利用
- `HomeLayoutMath.kt`: `homeGridColumns(isExpanded: Boolean)`の`isExpanded == true`時の返り値を8→4に変更（このレイアウトのアプリグリッドは常に4列固定のため）

## エラーハンドリング

- アプリ数が12個未満の場合: 余ったスロットは空欄（`Spacer`）のまま、既存`AppGrid`の挙動を踏襲
- アプリ数が12個を超える場合: `apps.take(12)`で切り詰め、13個目以降は非表示（今回はページングやスクロールは対象外）

## テスト方針

- レイアウトの見た目・各カードの余白感はAndroid Studioのfoldable（展開状態）エミュレータ/実機で目視確認
- `homeGridColumns`の返り値変更のみ既存の単体テストがあれば追従して更新
