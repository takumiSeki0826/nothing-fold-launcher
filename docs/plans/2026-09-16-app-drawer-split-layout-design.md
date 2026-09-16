# Z Fold 8 展開時 検索画面（AppDrawer）左右分割レイアウト

## 背景

`AppDrawer`（検索画面）は`isExpandedWidth`による分岐を持たず、常に`fillMaxSize()`で全幅を使う1カラム構成になっている。Z Fold 8展開時は画面幅いっぱいに検索結果リストが広がり、片手操作がしづらい。ホーム画面側は既に`isExpandedWidth`（[[2026-09-14-fold-expanded-screen-layout-design]]）で左右分割レイアウトを持っているため、検索画面も同様の分岐を導入する。

## スコープ

`AppDrawer.kt`の`isExpandedWidth == true`時のみ新しいレイアウトを追加する。compact（折りたたみ時）のレイアウトは変更しない。

## レイアウト

```
┌───────────────┬─────────────────────────┐
│               │  [Search            ]    │
│  ★App ★App ★App │  App A                    │
│  ★App ★App ★App │  App B                    │
│  ★App ★App ★App │  App C          [A]       │
│  ★App ★App ★App │  ...            [B]       │
│               │                 [C]       │
│  お気に入り(3列)  │  検索バー＋結果リスト＋      │
│               │  アルファベットインデックス   │
└───────────────┴─────────────────────────┘
   左 weight(4f)         右 weight(6f)
```

- 左右比率: ホーム画面展開時レイアウトと同じ`weight(4f)` / `weight(6f)`を踏襲
- **左カラム**: `viewModel.homeApps`（お気に入り、空なら既存の`defaultFavorites`で自動補完）を3列の`AppGrid`で表示。最大12件（`DEFAULT_HOME_APP_COUNT`）なので3列×4行でスクロール不要
  - タップ/長押しは`AppDrawer`が受け取る`onAppClick`/`onAppLongClick`をそのまま使う（ホーム画面と同一挙動、ノブ割り当て中の分岐も含めてMainActivity側のロジックを再利用）
- **右カラム**: 既存の検索バー（`OutlinedTextField`）＋検索結果`LazyColumn`＋`AlphabetIndexBar`をそのまま配置。ロジック変更なし
- 検索文字入力中も左のお気に入りグリッドは表示したままにする（レイアウト切り替えなし）
- 下スワイプで閉じるジェスチャーは既存通り右カラムの結果リストの`nestedScroll`にのみ付く。左のお気に入りグリッドでは効かないが、右側で閉じられるため許容する

## 変更ファイル

- `AppDrawer.kt`:
  - `isExpandedWidth: Boolean`、`homeApps: List<AppInfo>`、`iconColorFor: (AppInfo) -> IconPaletteColor?`をパラメータに追加
  - `isExpandedWidth == true`のとき、外側を`Row`にして左（お気に入り`AppGrid`、3列）／右（既存の検索`Column`をweight化）に分割
  - `isExpandedWidth == false`のときは現状の`Column`レイアウトのまま変更なし
- `HomeScreen.kt`: `private fun AppGrid(...)`を`internal fun AppGrid(...)`に変更し、`AppDrawer.kt`から再利用できるようにする
- `MainActivity.kt`: `AppDrawer`呼び出し（332行目付近）に`isExpandedWidth`（117行目で取得済みの値）、`viewModel.homeApps`、既存の`iconColorFor`ロジックを渡す

## エラーハンドリング / エッジケース

- 折りたたみ時（`isExpandedWidth == false`）は変更なし
- お気に入りが12件未満の場合: `AppGrid`が余ったスロットを空欄（`Spacer`）にする既存挙動をそのまま踏襲
- 左のお気に入りグリッドは下スワイプでは閉じない（新規の制約として許容）

## テスト方針

- 新規の数値ロジック（列数・比率など）は既存の`AppGrid`/`homeGridColumns`をそのまま再利用するため、追加の単体テストは最小限に留める
- レイアウトの見た目はAndroid Studioのfoldable（展開状態）エミュレータ/実機で目視確認する
