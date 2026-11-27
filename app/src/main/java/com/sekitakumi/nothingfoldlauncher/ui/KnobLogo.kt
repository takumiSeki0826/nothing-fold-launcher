package com.sekitakumi.nothingfoldlauncher.ui

/**
 * The single place that ties a home-knob logo to its app. To swap the app behind a logo,
 * change [packageName]; to add a logo, add an entry (and a default in HomeKnobAssignmentStore).
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
    CLAUDE(
        packageName = "com.anthropic.claude",
        displayName = "Claude",
        pattern = listOf(
            "...#...",
            ".#.#.#.",
            "..###..",
            "#######",
            "..###..",
            ".#.#.#.",
            "...#...",
        ),
    ),
}

fun knobLogoFor(packageName: String?): KnobLogo? = KnobLogo.values().firstOrNull { it.packageName == packageName }
