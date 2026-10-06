package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class KanaRomanizerTest {

    private fun r(s: String) = romanizeKana(s)

    @Test
    fun `hiragana and katakana give the same romaji`() {
        assertEquals("hitsujibungaku", r("ひつじぶんがく"))
        assertEquals("hitsujibungaku", r("ヒツジブンガク"))
    }

    @Test
    fun `uses hepburn spelling`() {
        assertEquals("shi chi tsu fu ji", listOf("し", "ち", "つ", "ふ", "じ").joinToString(" ") { r(it) })
        assertEquals("sushi", r("すし"))
    }

    @Test
    fun `particle ha stays ha`() {
        assertEquals("sayonaraha", r("さよならは"))
    }

    @Test
    fun `handles youon digraphs`() {
        assertEquals("kyou", r("きょう"))
        assertEquals("sha", r("しゃ"))
        assertEquals("chu", r("ちゅ"))
        assertEquals("jo", r("じょ"))
        assertEquals("ryokou", r("りょこう"))
    }

    @Test
    fun `doubles consonant after small tsu`() {
        assertEquals("gakkou", r("がっこう"))
        assertEquals("zasshi", r("ざっし"))
        assertEquals("matcha", r("まっちゃ"))
    }

    @Test
    fun `small tsu at the end or before a vowel is dropped`() {
        assertEquals("a", r("あっ"))
        assertEquals("a", r("っあ"))
    }

    @Test
    fun `n is plain n`() {
        assertEquals("ramen", r("らーめん"))
        assertEquals("kanpai", r("かんぱい"))
    }

    @Test
    fun `long vowel mark is dropped`() {
        assertEquals("emoshon", r("エモーション"))
        assertEquals("ramen", r("ラーメン"))
    }

    @Test
    fun `handles foreign sound combinations`() {
        assertEquals("fairu", r("ファイル"))
        assertEquals("ti", r("ティ"))
        assertEquals("di", r("ディ"))
        assertEquals("wi", r("ウィ"))
        assertEquals("va", r("ヴァ"))
        assertEquals("she", r("シェ"))
        assertEquals("je", r("ジェ"))
        assertEquals("che", r("チェ"))
    }

    @Test
    fun `maps japanese punctuation`() {
        assertEquals("ne.", r("ね。"))
        assertEquals("a,i", r("あ、い"))
        assertEquals("a i", r("あ・い"))
    }

    @Test
    fun `leaves kanji and latin untouched`() {
        assertEquals("声", r("声"))
        assertEquals("Four o'clock", r("Four o'clock"))
        assertEquals("四時niha", r("四時には"))
        assertEquals("Dpointo", r("Dポイント"))
    }

    @Test
    fun `empty stays empty`() {
        assertEquals("", r(""))
    }
}
