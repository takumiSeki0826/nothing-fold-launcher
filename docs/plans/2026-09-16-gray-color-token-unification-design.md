# グレー色トークン統一 設計

## 背景

パネル・UIコンポーネントで使われているグレーが、ファイルごとに直書きのhexで場当たり的に増えており、統一感がないという指摘から始まった。実際に洗い出すと、`Theme.kt` に定義された `NothingColorScheme` はどのコンポーネントからも参照されておらず（`MaterialTheme.colorScheme` 経由の参照がゼロ）、すべての色がファイル単位のリテラルとして直書きされていた。

グレーだけで8種類が混在:
`#8A8A8A`（テーマsecondary・IconPaletteのGRAY選択肢） / `Color.Gray`(`#888888`、サブテキスト系) / `#4A4A4A` / `#3A3A3A` / `#333333` / `#2A2A2A` / `#1A1A1A` / `#111111`

この中から「暗めのトーンで統一」する方針で整理する。

## 統一方針

役割ベースで整理し、3つの役割のうち実際に手を入れるのは2つ:

1. **テキスト系グレー**（`Color.Gray` `#888888`、サブラベル・プレースホルダーなど）: **現状維持**。可読性のため、黒背景に対するコントラストを下げたくない。
2. **アイコンパレットの選択色**（`#8A8A8A`、`IconPaletteColor.GRAY`）: **対象外**。これはUIクロームの色ではなく、ユーザーがアプリアイコンの背景として選べる4色のうちの1つという別レイヤーの意味を持つため、今回の統一スコープに含めない。
3. **パネル背景**: `#111111` → トークン化のみ（値は変更なし）
4. **非活性・枠線・トラック・未点灯ドット**: `#4A4A4A` `#3A3A3A` `#333333` `#2A2A2A` `#1A1A1A` の5値を **`#2A2A2A` に統合**

`#2A2A2A` を選んだ理由: 現状値の中間的な濃さで、黒背景の上でも未点灯ドットやノブ輪郭としてまだ視認できる濃さを保ちつつ、指示通り「暗め」に寄せられるため。ダイアログの `containerColor = Color.Black` は今回のスコープ外（グレーではなく黒そのものであり、別の検討事項）。

## トークン定義

新規ファイル `app/src/main/java/com/sekitakumi/nothingfoldlauncher/ui/theme/Color.kt` に追加する（既存の `Theme.kt` 内の色定義とは分離し、コンポーネント側から直接参照できるようにする）:

```kotlin
object NothingGrays {
    val PanelSurface = Color(0xFF111111)
    val Inactive = Color(0xFF2A2A2A)
}
```

## マッピング（置き換え対象）

| 現状の値 | 新トークン | 使用箇所 |
|---|---|---|
| `#111111` | `NothingGrays.PanelSurface` | `CalendarWidget.kt`（パネル背景）, `NowPlayingWidget.kt`（パネル背景） |
| `#333333` | `NothingGrays.Inactive` | `HomeScreen.kt`（`APP_ICON_BACKGROUND_COLOR`）, `RotaryKnob.kt`（`KNOB_FILL_COLOR`）, `HomeKnobSettingsMenu.kt`（ディバイダー）, `EqKnobSettingsMenu.kt`（ディバイダー） |
| `#4A4A4A` | `NothingGrays.Inactive` | `StatusIcons.kt`（電池バー未点灯）, `RotaryKnob.kt`（`KNOB_OUTLINE_COLOR`）, `CalendarWidget.kt`（`DOT_COLOR`） |
| `#3A3A3A` | `NothingGrays.Inactive` | `NowPlayingWidget.kt`（未点灯ドット） |
| `#2A2A2A` | `NothingGrays.Inactive` | `FaderSlider.kt`（トラック背景） |
| `#1A1A1A` | `NothingGrays.Inactive` | `FaderSlider.kt`（塗りつぶし） |

変更しないもの: `Color.Black`／`#000000`（画面背景・ダイアログ背景）、`Color.White`／`#F5F5F5`（主要テキスト）、`#D1432B`（アクセント赤）、`Color.Gray`／`#888888`（サブテキスト）、`#8A8A8A`（アイコンパレットのGRAY選択肢）。

## 影響ファイル

### 新規
| ファイル | 役割 |
|---|---|
| `ui/theme/Color.kt` | `NothingGrays` オブジェクト定義 |

### 変更
| ファイル | 変更内容 |
|---|---|
| `ui/StatusIcons.kt` | 電池バー未点灯色を `NothingGrays.Inactive` に置き換え |
| `ui/CalendarWidget.kt` | パネル背景を `NothingGrays.PanelSurface` に、未点灯ドット色を `NothingGrays.Inactive` に置き換え |
| `ui/NowPlayingWidget.kt` | パネル背景を `NothingGrays.PanelSurface` に、未点灯ドット色を `NothingGrays.Inactive` に置き換え |
| `ui/RotaryKnob.kt` | ノブ本体色・輪郭色を `NothingGrays.Inactive` に置き換え |
| `ui/HomeScreen.kt` | アプリアイコン背景色を `NothingGrays.Inactive` に置き換え |
| `ui/FaderSlider.kt` | トラック背景・塗りつぶし色を `NothingGrays.Inactive` に置き換え |
| `ui/HomeKnobSettingsMenu.kt` | ディバイダー色を `NothingGrays.Inactive` に置き換え |
| `ui/EqKnobSettingsMenu.kt` | ディバイダー色を `NothingGrays.Inactive` に置き換え |

## テスト方針

見た目だけの変更でロジック変更を伴わないため、新規ユニットテストは不要。既存のテスト（`HomeGestureMathTest`、`HomeLayoutMathTest`、`IconPaletteTest`、`NothingWallpaperRendererTest`）に影響がないことを確認する。

実機（Galaxy Z Fold8）での手動確認:
- 展開時・折りたたみ時の両方で、パネル背景（カレンダー・NowPlaying）とアイコン背景・ノブ・フェーダー・ディバイダーの見た目が統一されたグレーになっていること
- 未点灯ドット（カレンダー・NowPlaying）が黒背景に対してまだ視認できる濃さであること
- サブテキスト（プレースホルダー・「Close」など）の明るさが変わっていないこと
