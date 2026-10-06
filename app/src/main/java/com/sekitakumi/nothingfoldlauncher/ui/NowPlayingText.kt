package com.sekitakumi.nothingfoldlauncher.ui

private const val TITLE_SEPARATOR = " - "

private fun Char.isJapanese(): Boolean =
    this in 'ぁ'..'ゟ' || this in '゠'..'ヿ' || this in '一'..'鿿' || this in '＀'..'｟'

private fun String.hasJapanese(): Boolean = any { it.isJapanese() }

private fun String.hasAsciiLetter(): Boolean = any { it in 'a'..'z' || it in 'A'..'Z' }

/**
 * 音楽プレーヤーの曲名・アーティスト名をドット書体向けに整える。
 * 1. 日本語を含まなければそのまま。
 * 2. 「 - 」で区切った中に、日本語を含まず英字を含む部分があれば、最初のそれだけを使う（`四時にはね。 - Four o'clock` → `Four o'clock`）。
 * 3. 無ければ、かなだけをローマ字にする（漢字は残る）。
 */
fun nowPlayingDisplayText(text: String): String {
    if (!text.hasJapanese()) return text
    val latinSegment = text.split(TITLE_SEPARATOR)
        .firstOrNull { !it.hasJapanese() && it.hasAsciiLetter() }
    return latinSegment?.trim() ?: romanizeKana(text)
}
