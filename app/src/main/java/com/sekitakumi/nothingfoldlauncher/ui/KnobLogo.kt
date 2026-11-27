package com.sekitakumi.nothingfoldlauncher.ui

/**
 * The single place that ties a home-knob logo to its app. To swap the app behind a logo,
 * change [packageName]; to add a logo, add an entry here (to make it a default, also add a
 * migration step in HomeKnobAssignmentStore).
 * Patterns are dot-matrix art where '#' is a lit dot.
 */
enum class KnobLogo(
    val packageName: String,
    val displayName: String,
    val pattern: List<String>,
    /** Fixed grid pitch in dp; 0 scales the pattern to the standard logo size instead. */
    val cellDp: Float = 0f,
) {
    YOUTUBE(
        packageName = "com.google.android.youtube",
        displayName = "YouTube",
        pattern = listOf(
            "#.....",
            "###...",
            "#####.",
            "######",
            "#####.",
            "###...",
            "#.....",
        ),
    ),
    KINDLE(
        packageName = "com.amazon.kindle",
        displayName = "Kindle",
        pattern = KINDLE_PATTERN,
        cellDp = KINDLE_CELL_DP,
    ),
}

fun knobLogoFor(packageName: String?): KnobLogo? = KnobLogo.values().firstOrNull { it.packageName == packageName }

/** Dim "+" shown on a home knob that has no app yet; tapping it opens the app picker. */
val EMPTY_KNOB_PATTERN = listOf(
    "..#..",
    "..#..",
    "#####",
    "..#..",
    "..#..",
)

// Lowercase dot-matrix glyphs for the "kindle" wordmark (the app font only has capitals).
private val LOWERCASE_GLYPHS = mapOf(
    'k' to listOf("#...", "#...", "#..#", "#.#.", "##..", "#.#.", "#..#"),
    'i' to listOf("#", ".", "#", "#", "#", "#", "#"),
    'n' to listOf("....", "....", "###.", "#..#", "#..#", "#..#", "#..#"),
    'd' to listOf("...#", "...#", ".###", "#..#", "#..#", "#..#", ".###"),
    'l' to listOf("#", "#", "#", "#", "#", "#", "#"),
    'e' to listOf("....", "....", ".##.", "#..#", "####", "#...", ".###"),
)

/** Joins [LOWERCASE_GLYPHS] for [text] into one pattern, one blank column between letters. */
internal fun lowercaseWordPattern(text: String): List<String> =
    List(7) { row -> text.map { LOWERCASE_GLYPHS.getValue(it)[row] }.joinToString(".") }

private val KINDLE_PATTERN = lowercaseWordPattern("kindle")

/** Grid pitch of the "kindle" wordmark in dp; the one knob to turn for its size. */
private const val KINDLE_CELL_DP = 4.7f
