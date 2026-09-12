# Nothing Phone風 Galaxy Z Fold 8 ランチャーアプリ 設計

## 背景・目的

Galaxy Z Fold 8で使う自作ホームアプリ（ランチャー）を、Nothing Phoneのようなモノクロ・ドットマトリクス・ミニマルな雰囲気で作る。個人利用が目的で、Play Store申請は行わず、Android StudioからUSB経由で実機へ直接インストールする。

## スコープ（MVP）

- ホーム画面（アプリグリッド + 時計表示）
- アプリ一覧（ドロワー、検索付き）
- アプリ起動

以下はMVP対象外:
- ウィジェット配置
- アプリの並べ替え・フォルダ整理
- Fold 8の開閉状態（カバー画面/内側画面）に応じたレイアウト切り替え
- 壁紙・アイコンパック生成（別途「アイコン背景シェイプ生成」構想があったが、アイコン描画ユーティリティに統合し、独立ツールとしては作らない）

## 技術スタック

- Kotlin + Jetpack Compose（Android ネイティブ、単一Gradleモジュール）
- 配布: Play Store申請なし。Android StudioからUSBデバッグで実機へ直接インストール。

## アーキテクチャ

- **`MainActivity`**: `AndroidManifest.xml` に `CATEGORY_HOME` + `CATEGORY_DEFAULT` のintent-filterを設定し、ホームアプリとして選択可能にする。
- **`AppRepository`**: `PackageManager` から `ACTION_MAIN` + `CATEGORY_LAUNCHER` でインストール済みアプリ一覧を取得。
- **`AppListViewModel`**: 取得したアプリ一覧を `StateFlow` で公開し、検索・フィルタリングを担当。
- **`HomeScreen`（Compose）**: モノクロ・ドットマトリクス調の時計表示 + アプリグリッド。
- **`AppDrawer`（Compose）**: 全アプリ検索付き一覧。Nothing風のタイポグラフィと余白を意識したリストUI。
- **アイコン描画ユーティリティ**: 取得したアプリアイコンをグレースケール化 + 丸型マスクして統一感のある見た目にする。

## データフロー

1. `MainActivity` 起動
2. バックグラウンドコルーチンで `AppRepository` が `PackageManager` からアプリ一覧を取得
3. `AppListViewModel` が `StateFlow` で結果を公開
4. `HomeScreen` / `AppDrawer` がそれを購読して再描画
5. アプリタップ → `startActivity` で起動

並べ替え・ウィジェット配置はMVP対象外のため、このフローに含めない。

## エラーハンドリング

- アプリ一覧取得に失敗した場合: 空リスト + 簡単なエラーメッセージを表示。個人利用アプリのため過剰なリトライ処理は実装しない。
- 個別アプリのアイコン取得に失敗した場合: プレースホルダーグリフ（ドット柄の四角）を表示し、クラッシュさせない。
- ホームアプリとして未選択の状態のフォールバックは考慮しない（手動でデフォルトホームアプリを設定する前提）。

## テスト方針

- `AppRepository` のフィルタリング・検索ロジックなど純粋なロジックはJUnitでユニットテスト。
- 「ホームアプリとして選択 → ホーム画面表示 → アプリ起動」の一連の流れは実機（またはエミュレータ）で手動確認。
- Compose UIの網羅的なUIテストは個人アプリの規模的に見送り（YAGNI）。

## 検討した代替案

- **壁紙 + アイコンパック生成ツール（Web）**: 当初案。Good LockのIcon Pack Studioと組み合わせて標準ホーム画面の見た目だけを変える方式。よりスコープが小さく着手しやすいが、ユーザーの希望により「実際のランチャーアプリを自作する」方向に転換した。
- **Flutter / クロスプラットフォーム**: HOME intent-filterの受け取りやFold開閉検知のプラットフォーム連携が弱く、ランチャー用途とは相性が良くないため不採用。Z Fold 8専用の個人アプリであるため、Android公式ネイティブ（Kotlin + Compose）を採用。
