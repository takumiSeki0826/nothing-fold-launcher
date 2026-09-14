package com.sekitakumi.nothingfoldlauncher.ui

import com.sekitakumi.nothingfoldlauncher.data.AppInfo

const val ALPHABET_INDEX_THRESHOLD = 20

val ALPHABET_INDEX_LETTERS: List<Char> = ('A'..'Z').toList() + '#'

fun shouldShowAlphabetIndex(apps: List<AppInfo>): Boolean =
    apps.size >= ALPHABET_INDEX_THRESHOLD

fun bucketForLabel(label: String): Char {
    val first = label.trim().firstOrNull()?.uppercaseChar()
    return if (first != null && first in 'A'..'Z') first else '#'
}

fun letterIndexMap(apps: List<AppInfo>): Map<Char, Int> {
    val map = LinkedHashMap<Char, Int>()
    apps.forEachIndexed { index, app ->
        val bucket = bucketForLabel(app.label)
        if (bucket in 'A'..'Z' && bucket !in map) {
            map[bucket] = index
        }
    }

    // '#' groups everything outside A-Z, but a lowercase-sorted app list places
    // digit/symbol-prefixed labels *before* 'A' and non-Latin labels (e.g. Japanese)
    // *after* 'Z' - two disjoint groups sharing one bucket. Prefer the trailing
    // group (it sits right after 'Z' on the bar); fall back to the leading one
    // only if there's nothing after the alphabetic region.
    val lastLetterIndex = map.values.maxOrNull()
    val hashIndex = apps.withIndex()
        .filter { (index, app) -> bucketForLabel(app.label) == '#' && (lastLetterIndex == null || index > lastLetterIndex) }
        .map { it.index }
        .firstOrNull()
        ?: apps.withIndex().filter { (_, app) -> bucketForLabel(app.label) == '#' }.map { it.index }.firstOrNull()
    if (hashIndex != null) map['#'] = hashIndex

    return map
}

fun scrollIndexForLetter(letter: Char, indexMap: Map<Char, Int>, totalItems: Int): Int? {
    if (totalItems == 0) return null
    val startPos = ALPHABET_INDEX_LETTERS.indexOf(letter).coerceAtLeast(0)
    for (i in startPos until ALPHABET_INDEX_LETTERS.size) {
        indexMap[ALPHABET_INDEX_LETTERS[i]]?.let { return it }
    }
    return totalItems - 1
}

fun letterForBarPosition(relativeY: Float): Char {
    val clamped = relativeY.coerceIn(0f, 1f)
    val index = (clamped * ALPHABET_INDEX_LETTERS.size).toInt()
        .coerceIn(0, ALPHABET_INDEX_LETTERS.size - 1)
    return ALPHABET_INDEX_LETTERS[index]
}

// Niagara Launcher-style dock magnification: the touched letter scales up
// largest, with neighboring letters scaling progressively less, for a
// lens-like visual effect.
const val ALPHABET_INDEX_MAX_SCALE = 6.4f
const val ALPHABET_INDEX_MAGNIFY_RADIUS = 3

fun alphabetIndexLetterScale(distance: Int): Float {
    if (distance >= ALPHABET_INDEX_MAGNIFY_RADIUS) return 1f
    val t = 1f - (distance.toFloat() / ALPHABET_INDEX_MAGNIFY_RADIUS)
    return 1f + (ALPHABET_INDEX_MAX_SCALE - 1f) * t
}

// Roughly a fingertip's width (Android's standard touch target size), so the
// magnified letter clears the finger that's covering the bar itself.
const val ALPHABET_INDEX_FINGER_OFFSET_DP = 88

// Niagara Launcher-style follow: the magnified letter starts already
// shifted left by baseOffsetPx (roughly a fingertip's width) so the
// finger doesn't cover it, then tracks further leftward drag beyond
// that. Rightward movement eases it back toward the bar but never past
// it (0 = pinned to the bar's own x position).
fun alphabetIndexDragOffsetX(downX: Float, currentX: Float, baseOffsetPx: Float): Float =
    (baseOffsetPx + (currentX - downX)).coerceAtMost(0f)
