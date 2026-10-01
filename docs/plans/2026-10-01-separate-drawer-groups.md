# ドロワーグループとホームフォルダのデータ分離

## 背景

アプリ一覧画面（ドロワー）のグループとホーム画面（グリッド/ノブ）のフォルダは、現状 `AppFolder` / `HomeAppFolderStore`
（`AppListViewModel.folders` / `_folders`）を共有している（`2026-10-01-drawer-app-groups.md` の決定による）。
このため、ドロワー用の `excludeGroupedApps` がホームフォルダにも効いてしまい、TAK-124
（ホームフォルダをタップしても先頭アプリが起動しない）のような副作用が出た。
データ層を完全に分離し、この種の問題を構造的になくす。

TAK-124 は本設計に統合し、個別修正はしない。本設計の実装で根本原因（ドロワー用除外ロジックがホームフォルダに適用されること）が消える。

## 確定事項（再設計しない）

- ドロワーグループ用に新しいデータクラス・新しいSharedPreferencesストアを新設する。`AppFolder` / `HomeAppFolderStore` とは別物。
- 既存のホームフォルダ（Folder / SNS / AI / Pay）は移行せず、ホーム専用として残る。`HomeAppFolderStore` と保存データは一切変更しない。
- 1つのアプリは「ホームフォルダ」と「ドロワーグループ」の両方に同時所属できる（相互排他にしない）。ドロワーグループ同士の重複も許す（現状と同じ）。
- ドロワーで作ったグループはホームの並び順（`reconcileHomeOrder` / `HomeOrderStore`）に一切関与しない（自動追加されない）。
- `excludeGroupedApps` は、分離後はドロワーグループ所属アプリのみを対象にする。ホームフォルダ所属アプリはA-Zリストに引き続き表示される。
- `FolderEditDialog` / `FolderOverlay` / アイコン表示などのUIは共通Composableを再利用し、データソースだけ切り替える。
- 新しい依存ライブラリは追加しない。

## スコープ

含む: 新データクラス・ストア、`AppListViewModel` の拡張、`MainActivity` の編集対象・配線の分離、`AppDrawer` のprop型変更、純粋関数のテスト。

含まない: `HomeAppFolderStore` / `AppFolder` / `homeItems` / `HomeScreen` / `reconcileHomeOrder` / ノブフォルダ（`HomeKnobAssignmentStore`）の変更、既存データの移行。

## 既存実装の要点（`origin/master` 時点）

- `AppListViewModel`: `_folders`（`HomeAppFolderStore` 由来）が `folders` / `visibleApps`（`excludeGroupedApps`）/ `homeItems` / `currentValidRefs` の全部で共有されている。
  `addFolder` / `updateFolder` / `swapFolderPackages` / `deleteFolder` もこれ1本。
- `AppDrawer`: `folders: List<AppFolder>`、`onFolderClick` / `onFolderLongClick` を受け、先頭に `FolderIconRow` を出す。
- `MainActivity`:
  - `folderEditDialogTarget: String?`（`FolderEditDialog` を出す対象id。ホーム・ドロワー両方が同じ変数を使う）
  - `FolderEditTarget`（`HomeKnob(slot)` / `Grid(folderId)` / `NewGroup`、ドロワーを選択モードにして編集する対象）
  - `FolderOverlayState`（`Grid(folderId)` / `Knob(slot)`、`FolderOverlay` を出す対象）
  - `AppContextMenu` の「グループを作成」→ `FolderEditTarget.NewGroup` → `NewGroupDialog` → `viewModel.addFolder`
  - ドロワー側 `onFolderClick` は `FolderOverlayState.Grid(it.id)`、`onFolderLongClick` は `folderEditDialogTarget = folder.id`

## 設計

### 1. データ層

新規ファイル:

- `data/DrawerAppGroup.kt`

  ```kotlin
  data class DrawerAppGroup(
      val id: String,
      val name: String,
      val packageNames: List<String>,
  )
  ```

  `AppFolder` と同形だが別型にする。型が違うことで、ホーム用ロジックにドロワーグループを、ドロワー用ロジックにホームフォルダを
  誤って渡すとコンパイルエラーになる（今回の不具合の再発防止）。

- `data/DrawerAppGroupStore.kt`

  - SharedPreferences名: `"drawer_app_groups"`（`"home_app_folders"` と別ファイル。キー衝突は構造上起きない）
  - キー: `"group_order"`（id順リスト）、`"<id>_name"`、`"<id>_packages"`。エンコードは既存の `encodePackageList` / `decodePackageList` を流用。
  - API（`HomeAppFolderStore` と同じ形）: `getGroups()`, `addGroup(name, packageNames): DrawerAppGroup`,
    `updateGroup(id, name, packageNames)`, `deleteGroup(id)`
  - コンストラクタは `class DrawerAppGroupStore(private val prefs: SharedPreferences)` を主コンストラクタにし、
    `constructor(context: Context) : this(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))` を用意する。
    Robolectric等を追加できないため、テストでメモリ上のfake `SharedPreferences` を注入できるようにする（セクション6）。

- `data/GroupedApps.kt` の変更:

  - `groupedPackageNames(folders: List<AppFolder>)` → `groupedPackageNames(groups: List<DrawerAppGroup>)`
  - `excludeGroupedApps(apps, folders: List<AppFolder>, query)` → `excludeGroupedApps(apps, groups: List<DrawerAppGroup>, query)`
  - 検索中（`query` が空でない）は除外しない挙動は維持。
  - この2関数の呼び出し元は `AppListViewModel.visibleApps` のみ。ホーム側からは参照しない。

### 2. `AppListViewModel`

- コンストラクタに `drawerAppGroupStore: DrawerAppGroupStore` を追加（`MainActivity` で `by lazy { DrawerAppGroupStore(applicationContext) }` を生成して渡す。ViewModel factoryも更新）。
- 追加:
  - `private val _drawerGroups = MutableStateFlow(drawerAppGroupStore.getGroups())`
  - `val drawerGroups: StateFlow<List<DrawerAppGroup>>`
  - `addDrawerGroup(name, packageNames): DrawerAppGroup` / `updateDrawerGroup(id, name, packageNames)` /
    `deleteDrawerGroup(id)` / `swapDrawerGroupPackages(id, fromPackage, toPackage)`
    （実装は既存 `addFolder` / `updateFolder` / `deleteFolder` / `swapFolderPackages` と同じ形。並べ替えは既存の `swapHomeOrder` を流用）
- 変更:
  - `visibleApps` の `combine(..., _folders)` を `_drawerGroups` に差し替え、`excludeGroupedApps(..., drawerGroups, query)` を呼ぶ。
- 変更しない: `folders`, `_folders`, `homeItems`, `currentValidRefs`, `addFolder` / `updateFolder` / `swapFolderPackages` / `deleteFolder`。
  `_order` / `HomeOrderStore` はドロワーグループを一切知らない。

### 3. `MainActivity` の変更

「どのストアの、どのidか」を区別できるようにする。名前は既存コードの用語に合わせる。

1. ダイアログ対象: `var folderEditDialogTarget by remember { mutableStateOf<String?>(null) }` を型付きに置き換える。

   ```kotlin
   private sealed class FolderDialogTarget {
       data class Home(val folderId: String) : FolderDialogTarget()
       data class Drawer(val groupId: String) : FolderDialogTarget()
   }
   var folderDialogTarget by remember { mutableStateOf<FolderDialogTarget?>(null) }
   ```

   `FolderEditDialog` を出すブロックは `when (target)` で分岐し、データソースだけ切り替える。
   - `Home`: `folders` から検索し `viewModel.updateFolder` / `deleteFolder`（現状のまま）
   - `Drawer`: `drawerGroups` から検索し `viewModel.updateDrawerGroup` / `deleteDrawerGroup`
   - 見つからない場合は `folderDialogTarget = null`（現状と同じ）
   - 「アプリを編集」は `startGridFolderEdit`（Home）/ `startDrawerGroupEdit`（Drawer）を呼ぶ。

   既存の `folderEditDialogTarget = ...` の設定箇所（ホームの `onFolderClick`/`onFolderLongClick`、`onAddFolder`、`onEditFolder`、`startGridFolderEdit`）は
   `FolderDialogTarget.Home(id)` に、ドロワーの `onFolderLongClick` は `FolderDialogTarget.Drawer(id)` に置き換える。

2. 選択モード対象 `FolderEditTarget` に `data class Drawer(val groupId: String)` を追加する。
   `finishFolderEdit` で `FolderEditTarget.Drawer` のとき `viewModel.updateDrawerGroup(target.groupId, currentName, folderEditSelection.toList())`（`currentName` は `drawerGroups` から取得）。
   既存の `Grid` はホームフォルダ専用のまま。`startDrawerGroupEdit(group: DrawerAppGroup)` を `startGridFolderEdit` と同形で追加する。
   `when` の網羅性のため、`NewGroup` / `null` の分岐は維持。

3. オーバーレイ対象 `FolderOverlayState` に `data class Drawer(val groupId: String)` を追加する。
   `FolderOverlay` 表示ブロックで `Drawer` のとき `drawerGroups` を引き、`swapDrawerGroupPackages` を呼ぶ。
   ドロワーの `onFolderClick` は `FolderOverlayState.Drawer(it.id)`。ホームの `FolderOverlayState.Grid` / `Knob` は変更しない。

4. 「グループを作成」フロー:
   `AppContextMenu.onCreateGroup`（`FolderEditTarget.NewGroup` を立てる部分）はそのまま。
   `NewGroupDialog.onConfirm` の `viewModel.addFolder(name, newGroupSelection.toList())` を
   `viewModel.addDrawerGroup(name, newGroupSelection.toList())` に変える。
   `NewGroup` は定義上ドロワー専用になるので、コメントでその旨を残す。

5. `AppDrawer` 呼び出しに `drawerGroups`（`viewModel.drawerGroups.collectAsState()`）を渡す。`HomeScreen` に渡す `folders` は従来通り。

### 4. `AppDrawer` の `folders` prop

`DrawerAppGroup` 型にする（`AppFolder` の使い回しはしない）。

- `AppDrawer(folders: List<AppFolder>, onFolderClick: (AppFolder) -> Unit, onFolderLongClick: (AppFolder) -> Unit)` を
  `groups: List<DrawerAppGroup>, onGroupClick: (DrawerAppGroup) -> Unit, onGroupLongClick: (DrawerAppGroup) -> Unit` に改名・型変更する。
  内部の `FolderIconRow` も `DrawerAppGroup` を受け取る（`id` / `name` しか使っていないため変更は型名と変数名のみ）。
- 理由: 型で「ドロワー用データ」と明示でき、`AppDrawer` にホームフォルダを渡す誤配線を防げる。`FolderOverlay` / `FolderEditDialog` は
  `name` / `apps` / `appCount` といったプリミティブを受け取るため型変更の影響を受けず、共通Composableのまま再利用できる。
- `FolderIconRow` が `AppFolder` に依存しなくなるため、`AppDrawer.kt` から `AppFolder` のimportが消える。

### 5. 既存データの扱い

- 移行なし。`home_app_folders` 内のフォルダ（既存4つ、および旧仕様でドロワーから作ったグループがあればそれも）はホームフォルダとして残る。
  ドロワーのグループ行は空の状態から始まる。
- 旧仕様でドロワーから作ったグループがホームフォルダとして残る件は許容する（ユーザーが `FolderEditDialog` の削除で消せる）。
- 既存4フォルダ所属アプリは、分離後はA-Zリストに再び表示される（確定事項どおり）。

### 6. テスト方針

JVMユニットテスト（既存と同じ `junit:junit:4.13.2` のみ。依存は追加しない）。

- `data/GroupedAppsTest.kt`（更新）: 引数を `DrawerAppGroup` に置き換えた上で、既存ケース
  （空 / 所属アプリ除外 / 複数グループ重複 / 検索中は除外しない）を維持する。
- `data/DrawerAppGroupStoreTest.kt`（新規）: メモリ上の fake `SharedPreferences` を注入し、
  `add` → `getGroups` の往復、`update`（名前・アプリ変更、並び順維持）、`delete`（order・name・packages キーの除去）、
  追加順の保持、存在しないidの `update`/`delete` が他のグループを壊さないこと、
  `home_app_folders` とは別のprefs名であること（同じfake基盤上で `HomeAppFolderStore` 相当のキーに触れないこと）を確認する。
- 分離の回帰テスト（`GroupedAppsTest` に追加）: ホームフォルダ（`AppFolder`）所属アプリは `excludeGroupedApps` に渡す手段がなく
  （型が違う）、ドロワーグループが空ならA-Zリストが全件のままであることを確認する（TAK-124の根本原因に対応）。
- `AppListViewModel` は Android 依存（`AppRepository` 等）が強く既存テストもないため、ViewModelのユニットテストは対象外。
  代わりに純粋関数に寄せたロジック（除外・並べ替え）をテストする。
- 実機確認（Galaxy Z Fold8、展開/折りたたみ両方）: ホームフォルダのタップで先頭アプリが起動する / ドロワーでグループ作成・編集・削除・
  オーバーレイ内並べ替えができる / ドロワーグループ作成でホームの並びが変わらない / ホームフォルダ所属アプリがA-Zに残る。

### 7. 実装チケットの分割案

1. `DrawerAppGroup` + `DrawerAppGroupStore` + `GroupedApps` の型変更 + テスト
2. `AppListViewModel` への `drawerGroups` と CRUD の追加、`visibleApps` の差し替え
3. `AppDrawer` のprop型変更と `MainActivity` の `FolderDialogTarget` / `FolderEditTarget.Drawer` / `FolderOverlayState.Drawer` / 作成フロー配線

## 変更ファイル一覧（実装時）

- 新規: `data/DrawerAppGroup.kt`, `data/DrawerAppGroupStore.kt`, `test/.../data/DrawerAppGroupStoreTest.kt`
- 変更: `data/GroupedApps.kt`, `ui/AppListViewModel.kt`, `ui/AppDrawer.kt`, `MainActivity.kt`, `test/.../data/GroupedAppsTest.kt`
- 変更なし: `data/AppFolder.kt`, `data/HomeAppFolderStore.kt`, `data/HomeOrder.kt`, `ui/HomeScreen.kt`, `ui/FolderEditDialog.kt`, `ui/FolderOverlay.kt`

## 関連

- `2026-10-01-drawer-app-groups.md`: 「`AppFolder` / `HomeAppFolderStore` を共有する」決定を含む。本設計がその部分を置き換える
  （ドロワーのアイコン行・選択モード・検索中は除外しない、といった他の決定は引き継ぐ）。
- TAK-124: 本設計に統合。
