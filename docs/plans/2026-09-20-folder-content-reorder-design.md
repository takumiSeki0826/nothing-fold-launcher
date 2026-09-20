# フォルダ内アプリの並び替え 設計書

## 背景

ホーム画面のアプリ/フォルダ自体の並び替え（[[2026-09-17-home-grid-reorder-design.md]]）は実装済みだが、フォルダを開いた（`FolderOverlay`）中身のアプリの並び替え機能はスコープ外として未実装だった。フォルダ内のアプリ順を自由に並び替えたいという要望に対応する。

対象は「フォルダ内のアプリの並び替え」のみで、フォルダ自体（複数フォルダの配置順）の並び替えは対象外（既存の`HomeOrderStore`の仕組みで対応済み）。

`FolderOverlay`は以下2種類の「フォルダ」から共通で使われている点に注意する：
- ホーム画面グリッド上の通常フォルダ（`AppFolder`、`HomeAppFolderStore`に永続化）
- ジョグホイール（ノブ）の長押し割り当てフォルダ（`HomeKnobAssignmentStore`に永続化）

今回はこの両方に並び替えを適用する。

## 1. 操作方法

ホーム画面のアプリ/フォルダ並び替え（[[2026-09-17-home-grid-reorder-design.md]]）と同じジェスチャーに統一する。

- 長押し→そのまま動かさず離す：現状維持で何もしない（`FolderOverlayAppTile`の`onLongClick`は空のまま）
- 長押し→動かす：ドラッグ開始。指に追従してタイルが拡大表示され、通過先のスロットがハイライトされる
- ドロップ：離した位置に最も近いスロットと入れ替える（スワップ方式。空きスロットは`FolderOverlay`のグリッドには存在しないため「移動」は発生しない）

タップ（アプリ起動）・長押し（現状は何もしない）との判別は、ホーム画面と同じ`dragReorderable`ジェスチャーロジックをそのまま利用する。

## 2. ドラッグロジックの共通化

`HomeScreen.kt`内にprivateで実装されている`Modifier.dragReorderable`と`nearestSlotIndex`を、`AppGrid`と`FolderOverlay`の両方から使える共通ファイル（`ui/DragReorder.kt`想定）に切り出す。ロジック自体は変更しない。

`FolderOverlay`側は`AppGrid`と同様に、各タイルの位置を`onGloballyPositioned`で記録し、ドラッグ中は`graphicsLayer`で追従・拡大表示、ホバー中のタイルに枠線を出す。

## 3. データフロー・永続化

`FolderOverlay`に`onReorder: (from: AppInfo, to: AppInfo?) -> Unit`を追加する。

呼び出し元（`MainActivity.kt`）でどちらの永続化先に保存するかを分岐する必要があるため、現状の`FolderOverlayState`（単純なdata class）をsealed classにする：

```kotlin
private sealed class FolderOverlayState {
    data class Grid(val folderId: String, val name: String, val apps: List<AppInfo>) : FolderOverlayState()
    data class Knob(val slot: HomeKnobSlot, val name: String, val apps: List<AppInfo>) : FolderOverlayState()
}
```

- `Grid`の場合 → 並び替え後の`List<AppInfo>`を`packageNames`に変換し`viewModel.updateFolder(folderId, name, newOrder)`を呼ぶ
- `Knob`の場合 → `homeKnobAssignmentStore.setFolderPackages(slot, newOrder)`を呼び、`homeKnobPackageAssignments`のstateも更新する

`AppFolder.packageNames`のデータモデル自体（`List<String>`）は変更しない。並び順はリストの要素順そのもので表現する。

## 4. 「アプリを編集」画面での順序保持

フォルダの「アプリを編集」（`AppDrawer`でのチェックボックス追加/削除、`folderEditSelection: Set<String>`）を使うと、並び替えた順序が意図せず変わってしまわないか確認する。

Kotlinの`Set`に対する`+`/`-`演算子は内部的に`LinkedHashSet`ベースで実装されており、要素順を保持する仕様のため、現状の実装（`folder.packageNames.toSet()`からスタートし`+`/`-`で増減）は理論上「既存順序を維持しつつ新規は末尾に追加」という望ましい挙動に既になっているはずである。

実装着手時に、この挙動を実機/テストで確認する：

- 既に意図通りであれば実装変更は不要。回帰を防ぐためのユニットテストのみ追加する
- 崩れていた場合は、`Set<String>`をやめて明示的な順序付きリスト（例: 既存順序＋新規追加分のリスト）で管理するよう修正する

## 5. 画面状態（展開時／カバー画面）への配慮

`FolderOverlay`は画面幅に依存しない固定4カラムのダイアログであり、ドラッグ検出（タイル座標の記録と最近接判定）もダイアログのwindow内で完結するため、展開時・折りたたみ時（カバー画面）のどちらでも同じロジックで動作する想定。実機（Galaxy Z Fold8）の両状態でドラッグ操作を目視確認する。

## 6. 影響ファイル一覧（想定）

**新規:**
- `ui/DragReorder.kt` — `HomeScreen.kt`から`dragReorderable`/`nearestSlotIndex`を切り出した共通ロジック

**変更:**
- `ui/FolderOverlay.kt` — ドラッグ並び替えジェスチャーと見た目の追加、`onReorder`コールバック追加
- `ui/HomeScreen.kt` — `dragReorderable`/`nearestSlotIndex`を`DragReorder.kt`に移動（呼び出し側は変更なし）
- `MainActivity.kt` — `FolderOverlayState`のsealed化、`onReorder`の実装（`Grid`/`Knob`分岐）
- （4節の確認結果次第で）`folderEditSelection`まわりの順序保持ロジック

## テスト方針

- ドラッグジェスチャー自体はUIテストが難しいため、`AppGrid`と同様に手動＋実機（展開時・カバー画面両方）で確認する
- 並び替え確定後に正しい永続化メソッド（`updateFolder`/`setFolderPackages`）が正しい引数で呼ばれることを確認する
- 「アプリを編集」の順序保持について、[[2026-09-17-home-grid-reorder-design.md]]の`HomeOrderTest.kt`に近い形でユニットテストを追加する（4節の確認結果に応じて）

## スコープ外

- フォルダ自体（複数フォルダの配置順）の並び替え
- 折りたたみ時と展開時をまたぐような特別な並び替えUI（両状態とも同じロジックを共有する）
- フォルダ内アプリの長押し単体（動かさない場合）の挙動変更（コンテキストメニューなどは追加しない）
