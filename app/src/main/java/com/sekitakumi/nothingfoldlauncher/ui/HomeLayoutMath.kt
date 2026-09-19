package com.sekitakumi.nothingfoldlauncher.ui

private const val EXPANDED_WIDTH_THRESHOLD_DP = 600

fun isExpandedWidth(widthDp: Int): Boolean = widthDp >= EXPANDED_WIDTH_THRESHOLD_DP

fun homeGridColumns(isExpanded: Boolean): Int = 4

fun expandedGridMaxApps(): Int = 12

fun coverGridMaxItems(): Int = 8
