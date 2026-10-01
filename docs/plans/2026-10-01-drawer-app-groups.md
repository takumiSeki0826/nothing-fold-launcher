# アプリ一覧画面（ドロワー）のグループ機能

## 背景

ホーム画面には `AppFolder` / `HomeAppFolderStore` によるフォルダ機能があるが、アプリ一覧画面（`AppDrawer`）は
全アプリがA-Z順に並ぶだけで、よく使うアプリ群をまとめる手段がない。アプリ一覧画面でもアプリを
グループにまとめ、A-Zリストをすっきりさせたい。

## 確定事項（再設計しない）

- 既存の `AppFolder` / `HomeAppFolderStore` をそのまま流用する。クラス名・ファイル名・SharedPreferencesキー
  （`home_app_folders`, `folder_order`, `<id>_name`, `<id>_packages`）はリネームしない。
  ホーム画面で作ったフォルダはアプリ一覧画面にも、アプリ一覧画面で作ったグループもホーム画面にも、同じものとして表示される。
- 1つのアプリが複数のグループに同時に所属できる（`AppFolder.packageNames` 同士の重複を許す。現状のストアも制約なし）。
- アプリ一覧画面では各グループを先頭に1行のアイコンとして表示し、タップで既存 `FolderOverlay` を開く。
- いずれかのグループに所属するアプリは、アプリ一覧画面の通常のA-Zリストから除外する（ホーム画面側は変更しない）。
- グループ作成は既存の選択モード（`selectedPackages` / `onToggleSelected` / `onConfirmSelection`）を流用する。
- ホーム画面（`HomeScreen.kt`、`coverGridMaxItems` / `expandedGridMaxApps`、`reconcileHomeOrder`）のレイアウト・表示件数
  ロジックは一切変更しない。新規グループは `reconcileHomeOrder` により並び順リストの末尾に追加されるだけで、
  表示枠に収まらなければ自動ではホーム画面に出ない。

## スコープ

含む:
- `AppDrawer` 先頭へのグループアイコン行の追加
- グループ所属アプリのA-Zリストからの除外（純粋関数）
- 選択モードからのグループ作成（名前入力→確定）
- 純粋関数のユニットテスト

含まない:
- ホーム画面側の表示・ロジック変更
- グループのリネーム/削除/並べ替えのドロワー専用UI（既存の `FolderEditDialog` 経路はホーム設定メニュー側に残す）
- `AppFolder` / `HomeAppFolderStore` のスキーマ変更・リネーム、新規依存ライブラリ

## データフロー・クラス関係

```
HomeAppFolderStore ──getFolders()──▶ AppListViewModel._folders ──▶ folders: StateFlow<List<AppFolder>>
                                                  │
displayApps ─ query ─ hiddenApps ─▶ visibleApps   │
                    │                             ▼
                    └──────▶ drawerApps = combine(visibleApps, folders, query)
                                  = excludeGroupedApps(visibleApps, folders, query)   ← 新規（純粋関数）
                                                  │
                  MainActivity ◀──────────────────┘
                       │ AppDrawer.apps = drawerApps / groups = folders
                       ▼
                    AppDrawer(apps, folders, onGroupClick, …) ─▶ SearchColumn ─▶ LazyColumn
                                                                    ├─ GroupIconRow(folders)   ← 新規
                                                                    └─ AppRow(apps)
```

### `AppListViewModel` の変更

- `visibleApps` は既存のまま残す（ホーム側・フォルダオーバーレイ側が依存）。
- 新規 `val drawerApps: StateFlow<List<AppInfo>>`: `combine(visibleApps, _folders, _query)` に
  `excludeGroupedApps` を適用したもの。MainActivity は `apps`（= `visibleApps`、`appsByPackage` の元）をそのまま残し、`AppDrawer` の `apps` 引数にだけ `drawerApps` を渡す（2か所の `AppDrawer(` 呼び出しのうち通常表示側）。
- `addFolder` / `updateFolder` / `deleteFolder` は既存のものを流用（変更なし）。

### `AppDrawer` のprops変更

追加:
- `groups: List<AppFolder> = emptyList()`
- `onGroupClick: (AppFolder) -> Unit = {}`

`SearchColumn` にも同じ2つを素通しする。既存引数は変更しない（`selectedPackages` 等の選択モード引数含む）。
`apps` は呼び出し側で除外済みのリストを受け取るため、`AppDrawer` 自身はグループ所属を知らなくてよい。

## A-Zリストからの除外ロジック

`data/` 配下（例: 新規 `AppGroups.kt`）に純粋関数として切り出す。

```kotlin
fun groupedPackageNames(folders: List<AppFolder>): Set<String> =
    folders.flatMapTo(mutableSetOf()) { it.packageNames }

fun excludeGroupedApps(apps: List<AppInfo>, folders: List<AppFolder>, query: String): List<AppInfo> {
    if (query.isNotBlank()) return apps   // 検索中は全アプリを対象にする
    val grouped = groupedPackageNames(folders)
    return apps.filterNot { it.packageName in grouped }
}
```

- 検索中（`query` が空白でない）は除外しない。除外すると、グループ内のアプリが検索で見つからなくなるため。
  検索結果から起動/長押しメニューは通常通り使える。
- グループに属するが未インストール（`apps` に存在しない）のパッケージ名は単に無視される（`filterNot` のみ）。
- 非表示アプリ（`hiddenApps`）は `visibleApps` の段階で既に除外済み。グループ内に非表示アプリが残っていても、
  `FolderOverlay` 側は `appsByPackage` に存在すれば表示する既存挙動のまま（変更しない）。
- `letterIndexMap(apps)` / `scrollIndexForLetter` は除外後の `apps` を受け取るので、A-Zインデックスは自然に整合する。
  ただし `LazyColumn` の先頭にアイコン行（1 item）を追加するため、`scrollToItem` に渡すindexへ
  `+ (アイコン行を出していれば 1)` のオフセットを加える（`AppDrawer` 内の `scrollToItem` 呼び出し4箇所）。
  オフセット計算は純粋関数 `headerItemCount(groups, query)` に切り出してもよい。

## グループアイコン行のComposable設計

新規 `ui/GroupIconRow.kt`:

```kotlin
@Composable
fun GroupIconRow(
    groups: List<AppFolder>,
    onGroupClick: (AppFolder) -> Unit,
    modifier: Modifier = Modifier,
)
```

- 配置: `SearchColumn` の `LazyColumn` の先頭 `item { }`（検索欄・選択モードヘッダの下）。
  `LazyColumn` 内に置くことで、スワイプダウンで閉じるジェスチャ（`nestedScroll`）が引き続き効く。
- 中身: `LazyRow`（横スクロール）。各グループは `FolderOverlay` のタイルと同系統の見た目
  （`NothingGrays.Base` 背景の角丸タイル＋グループ名ラベル、グレー直書き禁止）。ホーム画面のフォルダアイコンの
  見た目に近づける。グループが0個なら行ごと出さない。
- タップ: `onGroupClick(folder)` → MainActivity が `folderOverlay = FolderOverlayState.Grid(folder.id)` をセットし、
  既存の `FolderOverlay` を開く（ドロワー上に重なる `Dialog` なのでホームへ戻る必要はない）。
  アプリ起動後は既存どおり `folderOverlay = null`。
- 長押し: v1では何もしない（`onLongClick = {}`）。リネーム/削除/アプリ編集はホーム画面設定メニュー経由の
  既存 `FolderEditDialog` を使う。
- 選択モード中（`selectedPackages != null`）はアイコン行を非表示にする（誤タップ防止）。
- 検索中（`query` が空白でない）もアイコン行を非表示にする（除外もしないため整合する）。

## グループ作成フロー

1. 開始トリガー: ドロワーでアプリを長押し → 既存のアプリメニュー（`openAppMenu`）に項目
   「グループを作成」を追加。選択すると、そのアプリを初期選択にして選択モードへ入る。
   （現状の長押しは既にメニュー表示に使われており、長押しで直接選択モードに入ると既存のお気に入り/
   非表示/リネーム操作が潰れるため、メニュー経由とする。）
2. 選択: 既存の `selectedPackages` / `onToggleSelected` でA-Zリスト上のアプリを複数選択。
   ただし `folderEditTarget`（Grid/HomeKnob）用の選択と区別するため、`FolderEditTarget` に
   `NewGroup` を追加する。選択モード中のヘッダ「N個選択中 / 完了」の「完了」を、
   `NewGroup` のときは「グループ作成」ラベルにする（`SearchColumn` のヘッダ文言をパラメータ化）。
3. 確定: 「グループ作成」押下 → 選択が0個なら何もしない（選択モード継続）。1個以上なら
   `FolderEditDialog` 相当の名前入力ダイアログ（`OutlinedTextField` 1つ、初期値 "Group"）を表示。
   確定で `viewModel.addFolder(name, selected.toList())` → 選択モード終了 → ドロワー通常表示に戻る。
   作成したグループはアイコン行に即反映される（`_folders` の更新による）。
4. キャンセル: ダイアログのキャンセルは選択モードに戻る。ドロワーを下スワイプで閉じたら既存どおり
   `onDrawerDismissed` で `folderEditTarget = null` となり選択も破棄。

## 複数グループ所属時の表示

- A-Zリストでは、いずれかのグループに所属していれば除外（所属数に関わらず1回だけ判定）。
- アイコン行では各グループが1つずつ表示されるため、同じアプリが複数グループの `FolderOverlay` に
  重複して出ること自体は許容（グループごとに独立したリスト）。
- グループ作成時の選択モードでは、既に別グループへ所属しているアプリはA-Zリストから除外されて
  いるため選択できない（検索中は除外されないので、検索経由なら複数所属にできる）。
  v1ではこの挙動を許容し、複数所属は「検索から選択」で行う運用とする。

## 実機で確認すべき項目

展開（大画面）・折りたたみ（カバー画面）の両方で確認する。

- [ ] グループ0個のとき、アイコン行が出ずレイアウトが従来と同一
- [ ] グループありで先頭にアイコン行が出て、横スクロール/タップで `FolderOverlay` が開く
- [ ] 展開時: ジョグホイール + 検索カラムの2ペイン構成でアイコン行が検索カラム内に収まる
- [ ] カバー時: 幅が狭くてもアイコンが欠けず横スクロールできる
- [ ] グループ所属アプリがA-Zリストに出ない / 検索すると出る
- [ ] A-Zインデックスバー・ジョグホイールのジャンプ位置が（アイコン行ぶんずれず）正しい
- [ ] 下スワイプでドロワーを閉じるジェスチャがアイコン行の存在で阻害されない
- [ ] 長押し → 「グループを作成」→ 複数選択 → 名前入力 → 確定で作成され、アイコン行に即反映
- [ ] 作成したグループがホーム画面側のフォルダ一覧（末尾）にも同じものとして存在し、ホームの表示件数
      ロジック・並びが変わらない（枠外ならホームに出ない）
- [ ] ホーム画面で作ったフォルダがドロワーにも出て、所属アプリがA-Zから消える
- [ ] フォルダ内アプリの起動後にオーバーレイが閉じる
- [ ] 折りたたみ⇔展開の切り替え（回転/画面状態変化）中に選択モード・オーバーレイが破綻しない

## テスト方針

ユニットテスト（JVM、`app/src/test/...`、既存の `HomeOrderTest` 等と同じ配置）:

- `groupedPackageNames`
  - グループなし → 空集合
  - 複数グループにまたがる重複パッケージ → 集合として1回
- `excludeGroupedApps`
  - グループなし → 入力と同一
  - 所属アプリが除外され、順序は保たれる
  - 複数グループ所属のアプリも除外される
  - `apps` に存在しないパッケージ名がグループにあっても例外にならない
  - `query` が空白でない → 除外されない
- （切り出す場合）`headerItemCount` — グループ0個/あり、検索中/非検索中、選択モード中の組み合わせ

Composable（`GroupIconRow`、ダイアログ配線）は既存方針どおりユニットテスト対象外とし、
上記の実機確認項目で担保する。`AppListViewModel` は `HomeAppFolderStore`（Context依存）を
直接受けるため、ロジックは純粋関数側に寄せてテストする。

## 検討した代替案

- 長押しで直接選択モードに入る → 既存メニュー（お気に入り/非表示/リネーム）と衝突するため見送り
- 検索中も除外を維持する → グループ内アプリが検索で見つからなくなるため見送り
- ドロワー専用の新規ストア → 確定事項（既存 `HomeAppFolderStore` 流用）に反するため不採用
- `visibleApps` 自体を除外済みにする → ホーム/フォルダオーバーレイ側の `appsByPackage` に影響するため、
  別 `StateFlow`（`drawerApps`）として追加する
