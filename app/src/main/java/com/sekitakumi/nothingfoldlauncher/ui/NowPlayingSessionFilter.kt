package com.sekitakumi.nothingfoldlauncher.ui

/** 読書後にメディアとして残るだけで、音楽プレーヤー表示には不要なアプリ。 */
private val IGNORED_PACKAGES = setOf("com.amazon.kindle")

/** 再生中のセッションを優先し、無ければ先頭を選ぶ。一時停止で残ったセッションに居座られないため。 */
fun <T> pickNowPlayingSession(
    sessions: List<T>?,
    packageNameOf: (T) -> String,
    isPlayingOf: (T) -> Boolean,
): T? {
    val candidates = sessions?.filter { packageNameOf(it) !in IGNORED_PACKAGES }.orEmpty()
    return candidates.firstOrNull(isPlayingOf) ?: candidates.firstOrNull()
}
