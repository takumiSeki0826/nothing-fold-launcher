package com.sekitakumi.nothingfoldlauncher.ui

private val BASE_ROMAJI: Map<Char, String> = buildMap {
    fun put(kana: String, vararg romaji: String) = kana.forEachIndexed { i, c -> put(c, romaji[i]) }
    put("あいうえお", "a", "i", "u", "e", "o")
    put("かきくけこ", "ka", "ki", "ku", "ke", "ko")
    put("さしすせそ", "sa", "shi", "su", "se", "so")
    put("たちつてと", "ta", "chi", "tsu", "te", "to")
    put("なにぬねの", "na", "ni", "nu", "ne", "no")
    put("はひふへほ", "ha", "hi", "fu", "he", "ho")
    put("まみむめも", "ma", "mi", "mu", "me", "mo")
    put("やゆよ", "ya", "yu", "yo")
    put("らりるれろ", "ra", "ri", "ru", "re", "ro")
    put("わゐゑを", "wa", "i", "e", "o")
    put("がぎぐげご", "ga", "gi", "gu", "ge", "go")
    put("ざじずぜぞ", "za", "ji", "zu", "ze", "zo")
    put("だぢづでど", "da", "ji", "zu", "de", "do")
    put("ばびぶべぼ", "ba", "bi", "bu", "be", "bo")
    put("ぱぴぷぺぽ", "pa", "pi", "pu", "pe", "po")
    put("ゔ", "vu")
    put("ん", "n")
}

private val SMALL_YOUON: Map<Char, String> = mapOf('ゃ' to "ya", 'ゅ' to "yu", 'ょ' to "yo")
private val SMALL_VOWEL: Map<Char, String> = mapOf('ぁ' to "a", 'ぃ' to "i", 'ぅ' to "u", 'ぇ' to "e", 'ぉ' to "o")
private val PUNCTUATION: Map<Char, String> = mapOf('。' to ".", '、' to ",", '・' to " ")

private const val SMALL_TSU = 'っ'
private const val LONG_VOWEL_MARK = 'ー'

/** カタカナをひらがなにそろえる（ァ〜ヶ → ぁ〜ゖ）。 */
private fun Char.toHiragana(): Char = if (this in 'ァ'..'ヶ') this - 0x60 else this

/** 語尾の母音を除いた子音部分。`shi` → `sh`、`te` → `t`、`u` → `w`（う+小書き母音）。 */
private fun stemOf(romaji: String): String =
    if (romaji == "u") "w" else romaji.dropLast(1)

/**
 * ひらがな・カタカナをヘボン式のローマ字にする。漢字などかな以外の文字はそのまま残す。
 * 長音符「ー」は落とす（`エモーション` → `emoshon`）。
 */
fun romanizeKana(text: String): String {
    val chars = text.map { it.toHiragana() }
    val out = StringBuilder()
    var pendingSmallTsu = false
    var i = 0

    fun emit(romaji: String) {
        if (pendingSmallTsu) {
            val first = romaji.first()
            if (first.isLetter() && first !in "aiueon") out.append(if (romaji.startsWith("ch")) 't' else first)
            pendingSmallTsu = false
        }
        out.append(romaji)
    }

    while (i < chars.size) {
        val c = chars[i]
        val base = BASE_ROMAJI[c]
        when {
            c == SMALL_TSU -> {
                pendingSmallTsu = true
                i++
            }
            c == LONG_VOWEL_MARK -> i++
            base != null -> {
                val next = chars.getOrNull(i + 1)
                val youon = next?.let { SMALL_YOUON[it] }
                val smallVowel = next?.let { SMALL_VOWEL[it] }
                when {
                    youon != null && base.endsWith("i") && base.length > 1 -> {
                        val stem = stemOf(base)
                        val vowel = youon.last().toString()
                        emit(if (stem in setOf("sh", "ch", "j")) stem + vowel else stem + "y" + vowel)
                        i += 2
                    }
                    smallVowel != null && base != "n" && !(base.length == 1 && base != "u") -> {
                        emit(stemOf(base) + smallVowel)
                        i += 2
                    }
                    else -> {
                        emit(base)
                        i++
                    }
                }
            }
            else -> {
                pendingSmallTsu = false
                out.append(PUNCTUATION[c] ?: text[i].toString())
                i++
            }
        }
    }
    return out.toString()
}
