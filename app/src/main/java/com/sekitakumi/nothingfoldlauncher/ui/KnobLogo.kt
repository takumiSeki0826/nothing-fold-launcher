package com.sekitakumi.nothingfoldlauncher.ui

/**
 * The single place that ties a home-knob logo to its app. To swap the app behind a logo,
 * change [packageName]; to add a logo, add an entry here (and, to make it a default, in
 * HomeKnobAssignmentStore.DEFAULT_ASSIGNMENTS).
 * Patterns are dot-matrix art where '#' is a lit dot.
 */
enum class KnobLogo(val packageName: String, val displayName: String, val pattern: List<String>) {
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
