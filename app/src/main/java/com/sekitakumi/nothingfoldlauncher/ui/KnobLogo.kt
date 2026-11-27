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
    val pattern: List<String> = emptyList(),
    /** Lines of dot-matrix text drawn instead of [pattern] when non-empty. */
    val wordmark: List<String> = emptyList(),
    val wordmarkDotDp: Float = 0f,
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
        wordmark = listOf("KINDLE"),
        wordmarkDotDp = 2.4f,
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
