# アプリのアンインストール機能 設計書

## 背景

現状のランチャーには、アプリをアンインストールする手段がない。アプリを削除するにはホーム画面のランチャーから離れて端末の設定アプリを開く必要がある。既存のロングプレスメニュー（`AppContextMenu`：ホーム表示切替 / Hide / Rename / Change color）に「Uninstall」を追加し、ランチャーから直接アンインストールできるようにする。

## 1. 全体アーキテクチャ

- `AppContextMenu`（`ui/AppContextMenu.kt`）に `onUninstall: () -> Unit` パラメータを追加し、既存の Rename / Change color と並ぶ位置に「Uninstall」ボタンを追加する。
- `MainActivity.kt` 側で `onUninstall` に、`Intent.ACTION_DELETE`（data: `package:<packageName>`）を `startActivity` する処理を実装する。これは OS 標準のアンインストール確認ダイアログを起動するだけで、追加の権限（`REQUEST_DELETE_PACKAGES` 等）は不要。ランチャーはすでに HOME カテゴリの intent-filter を持つため、アプリ一覧取得に使っている `PackageManager` の可視性にも影響はない。
- OS ダイアログでユーザーが「アンインストール」を確定 → OS がアプリを削除 → ランチャーに `onResume()` で戻ってくる。そのタイミングで `AppListViewModel.refresh()` を呼び出し、アプリ一覧（ホーム・ドロワー）を再取得して消えたアプリを一覧から除去する。

## 2. UI

- ボタンラベルは他項目と統一して `"Uninstall"`（英語表記）。
- 押下すると即座に `onDismiss` でメニューを閉じ、`ACTION_DELETE` intent を発火してシステムのアンインストール確認ダイアログへ遷移する。
- ランチャー自身での独自確認ダイアログは挟まない（OS ダイアログでの確認のみ）。

## 3. エラーハンドリング / エッジケース

- システムアプリなどアンインストール不可能なアプリの場合、OS 側のダイアログが失敗を表示する。ランチャー側で事前に判定・分岐するロジックは持たない。
- ユーザーが OS ダイアログでキャンセルした場合も、`onResume()` の `refresh()` は無害に実行されるだけ（対象アプリはまだ存在するので一覧に変化はない）。
- `favorites` / `hiddenApps` / `labelOverrides` / アイコンカラー等の設定に残る、削除済みパッケージ名のエントリはクリーンアップしない。実際のアプリ一覧（`displayApps`）に存在しないパッケージは表示上自然に除外されるため、実害はない。

## 4. テスト方針

- `AppContextMenu` に渡した `onUninstall` コールバックがボタン押下で呼ばれることをユニットテストで確認する（既存の `onRename` 等のテストパターンを踏襲）。
- 実機（Galaxy Z Fold8）で折りたたみ・展開の両方の状態から、ロングプレス → Uninstall タップ → OS ダイアログ → 確定 → 一覧から消えることを確認する。
