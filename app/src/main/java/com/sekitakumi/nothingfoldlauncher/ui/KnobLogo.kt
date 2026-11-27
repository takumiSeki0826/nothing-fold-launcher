package com.sekitakumi.nothingfoldlauncher.ui

enum class KnobLogo { YOUTUBE, X }

private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
private const val X_PACKAGE = "com.twitter.android"

fun knobLogoFor(packageName: String?): KnobLogo? = when (packageName) {
    YOUTUBE_PACKAGE -> KnobLogo.YOUTUBE
    X_PACKAGE -> KnobLogo.X
    else -> null
}

// Dot-matrix art, '#' = lit dot.
fun knobLogoPattern(logo: KnobLogo): List<String> = when (logo) {
    KnobLogo.YOUTUBE -> listOf(
        "#.....",
        "###...",
        "#####.",
        "######",
        "#####.",
        "###...",
        "#.....",
    )
    KnobLogo.X -> listOf(
        "##...##",
        "###.###",
        ".#####.",
        "..###..",
        ".#####.",
        "###.###",
        "##...##",
    )
}
