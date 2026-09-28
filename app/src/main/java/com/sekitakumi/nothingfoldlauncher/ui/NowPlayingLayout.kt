package com.sekitakumi.nothingfoldlauncher.ui

const val NOW_PLAYING_HEADER_MIN_HEIGHT_DP = 48

fun nowPlayingHeaderHeightDp(textHeightDp: Int, hasControl: Boolean): Int {
    return maxOf(textHeightDp, NOW_PLAYING_HEADER_MIN_HEIGHT_DP)
}
