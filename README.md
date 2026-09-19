# nothing-fold-launcher

Galaxy Z Fold8向けに自作している、Nothing Phone風のミニマル・ドットマトリクス美学を持つAndroidランチャー。折りたたみ(カバー画面)・展開時の両方のレイアウトに対応する。

## 特徴

- **ホーム画面**: 時計・天気ウィジェット・カレンダーウィジェット・再生中メディア情報・音量/明るさスライダー・フォルダノブ・アプリグリッド
- **展開時(Fold8を開いた状態)専用レイアウト**: 左パネルに時計・天気・カレンダー・メディア情報、右パネルにスライダー・ノブ・アプリグリッドを配置
- **アプリドロワー**: 展開時はアルファベットジョグホイールでA-Zスクラブ、検索、お気に入り表示に対応
  - ジョグホイール右上にシステムステータスウィジェット(CPU/メモリ/ストレージ使用率、ネットワーク上り/下り速度)を表示。ネットワーク行はタップでspeedtest.netを開く
- **フォルダ機能**: ホームノブ/アプリグリッド上にフォルダを作成可能。フォルダに入っているアプリのアイコンにはオレンジのドットを表示
- **カスタムステータスバー**: バッテリー残量・Wi-Fi・モバイル電波強度・VPN/Tailscaleバッジを独自描画(OS標準ステータスバーは非表示)
- **ライブ壁紙**: ホーム画面と連動したドットカレンダーのロック画面壁紙
- **アプリ管理**: 非表示・リネーム・アンインストール、アプリアイコンのカラーパレット選択

## 技術スタック

- Kotlin + Jetpack Compose (Material 3)
- Kotlin Coroutines / StateFlow
- minSdk 33 / targetSdk 37 / compileSdk 37
- JVM 17

## ビルド・実行

### 前提

- JDK 17
- Android SDK (`adb`)
- 実機推奨: Galaxy Z Fold8(展開/折りたたみ両レイアウトの確認に実機が必要)

### コマンド

```bash
# ユニットテスト
./gradlew :app:testDebugUnitTest

# デバッグビルド
./gradlew :app:assembleDebug

# 実機/エミュレータにインストール
./gradlew :app:installDebug
```

インストール後、Androidの設定からデフォルトのホームアプリとしてこのランチャーを選択する。

## プロジェクト構成

```
app/src/main/java/com/sekitakumi/nothingfoldlauncher/
├── MainActivity.kt               # エントリーポイント、画面遷移・状態管理の配線
├── NowPlayingListenerService.kt  # 再生中メディア情報取得用の通知リスナー
├── data/                         # アプリ一覧・フォルダ・お気に入り・各種設定の永続化層
├── ui/                           # Composable画面・ウィジェット・純粋関数(*Math.kt)
└── wallpaper/                    # ホーム/ロック画面と連動するライブ壁紙
```

配色は`ui/theme/Color.kt`の`NothingGrays`トークンに統一している(詳細はプロジェクトの`CLAUDE.md`を参照)。

## 設計ドキュメント

機能追加のたびに、背景・スコープ・アーキテクチャ・テスト方針をまとめた設計docを`docs/plans/`に日付付きMarkdownで残している。

## テスト方針

`ui/*Math.kt`などの純粋関数はTDDでユニットテスト(`app/src/test/`)を書く。Composableの見た目や実際のシステム挙動(センサー値・権限フローなど)は実機で確認する方針。
