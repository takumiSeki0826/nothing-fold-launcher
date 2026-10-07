package com.sekitakumi.nothingfoldlauncher.ui

import kotlin.math.ceil

/**
 * ドット書体と同じグリッドで描くアイコンの図柄。`#` = 点灯、`o` = 淡色、`.` = 空。
 */
object DotIcons {
    val WIFI = listOf(
        ".#######.",
        "#.......#",
        "..#####..",
        ".#.....#.",
        "...###...",
        "....#....",
    )

    val BOLT = listOf(
        "....#",
        "...#.",
        "..#..",
        "#####",
        "..#..",
        ".#...",
        "#....",
    )

    /** 塗りの4つが明るい点、輪郭だけの5つが淡色（ロゴと同じ配置）。 */
    val TAILSCALE = listOf(
        "ooo",
        "###",
        "o#o",
    )

    val SUN = listOf(
        "....#....",
        ".#.....#.",
        "...###...",
        "..#####..",
        "#.#####.#",
        "..#####..",
        "...###...",
        ".#.....#.",
        "....#....",
    )

    val PARTLY_CLOUDY = listOf(
        "...#.......",
        ".#.#.#..##.",
        "..###.#####",
        "..##.######",
        "..#########",
        ".#########.",
        "..#######..",
    )

    val CLOUD = listOf(
        "....###....",
        "..#######..",
        ".#########.",
        "###########",
        "###########",
        ".#########.",
    )

    val FOG = listOf(
        "###########",
        "...........",
        "###########",
        "...........",
        "###########",
    )

    val RAIN = listOf(
        "...###.....",
        ".#########.",
        "###########",
        ".#########.",
        "...........",
        "..#..#..#..",
        ".#..#..#...",
    )

    val SNOW = listOf(
        "....#....",
        ".#..#..#.",
        "..#.#.#..",
        "...###...",
        "#########",
        "...###...",
        "..#.#.#..",
        ".#..#..#.",
        "....#....",
    )

    val THUNDERSTORM = listOf(
        "...###.....",
        ".#########.",
        "###########",
        ".#########.",
        ".....##....",
        "....##.....",
        ".....#.....",
    )

    val DROP = listOf(
        "..#..",
        ".###.",
        ".###.",
        "#####",
        "#####",
        ".###.",
    )

    val UMBRELLA = listOf(
        "...###...",
        ".#######.",
        "#########",
        "....#....",
        "....#....",
        "..##.....",
    )

    val PLAY = listOf(
        "#....",
        "##...",
        "###..",
        "####.",
        "###..",
        "##...",
        "#....",
    )

    val PAUSE = List(7) { "##.##" }

    val BELL = listOf(
        "...#...",
        "..###..",
        ".#####.",
        ".#####.",
        ".#####.",
        "#######",
        "...#...",
    )

    val CHEVRON_RIGHT = listOf(
        "#...",
        ".#..",
        "..#.",
        "...#",
        "..#.",
        ".#..",
        "#...",
    )

    val CHECK_ON = List(7) { "#######" }

    val CHECK_OFF = listOf(
        "#######",
        "#.....#",
        "#.....#",
        "#.....#",
        "#.....#",
        "#.....#",
        "#######",
    )

    val STAR = listOf(
        "...#...",
        "..###..",
        "#######",
        ".#####.",
        "..###..",
        ".##.##.",
        ".#...#.",
    )
}

private const val SIGNAL_BAR_COUNT = 4
private const val SIGNAL_MIN_BAR_HEIGHT = 2

/** 4本の縦バー（高さ 2〜5 ドット）。[bars] 本目までを点灯、残りを淡色にする。 */
fun signalBarsPattern(bars: Int): List<String> {
    val lit = bars.coerceIn(0, SIGNAL_BAR_COUNT)
    val rows = SIGNAL_MIN_BAR_HEIGHT + SIGNAL_BAR_COUNT - 1
    val cols = SIGNAL_BAR_COUNT * 2 - 1
    return List(rows) { row ->
        buildString {
            for (col in 0 until cols) {
                val bar = col / 2
                val isBarCol = col % 2 == 0
                val height = SIGNAL_MIN_BAR_HEIGHT + bar
                append(
                    when {
                        !isBarCol -> '.'
                        row < rows - height -> '.'
                        bar < lit -> '#'
                        else -> 'o'
                    },
                )
            }
        }
    }
}

const val BATTERY_FILL_COLS = 8
const val BATTERY_FILL_ROWS = 3

/** 枠 + 右端のこぶ(淡い `x`) + 残量に応じて左から埋まる [BATTERY_FILL_COLS] 列の塗り。 */
fun batteryPattern(percent: Int): List<String> {
    val clamped = percent.coerceIn(0, 100)
    val filledCols = ceil(clamped * BATTERY_FILL_COLS / 100.0).toInt()
    val bodyCols = BATTERY_FILL_COLS + 4 // 枠2 + 隙間2
    val rows = BATTERY_FILL_ROWS + 4 // 枠2 + 隙間2
    val fillStartRow = (rows - BATTERY_FILL_ROWS) / 2
    return List(rows) { row ->
        buildString {
            for (col in 0..bodyCols) {
                val isNubRow = row in fillStartRow until fillStartRow + BATTERY_FILL_ROWS
                append(
                    when {
                        col == bodyCols -> if (isNubRow) 'x' else '.'
                        row == 0 || row == rows - 1 -> 'x'
                        col == 0 || col == bodyCols - 1 -> 'x'
                        row in fillStartRow until fillStartRow + BATTERY_FILL_ROWS &&
                            col in 2 until 2 + filledCols -> '#'
                        else -> '.'
                    },
                )
            }
        }
    }
}

private const val ICON_PITCH_RATIO = DOT_PITCH_RATIO

fun dotMatrixIconWidthPx(rows: List<String>, d: Float): Float =
    (rows[0].length - 1) * d * ICON_PITCH_RATIO + d

fun dotMatrixIconHeightPx(rows: List<String>, d: Float): Float =
    (rows.size - 1) * d * ICON_PITCH_RATIO + d
