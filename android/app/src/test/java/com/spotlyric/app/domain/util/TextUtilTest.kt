package com.spotlyric.app.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextUtilTest {

    @Test
    fun testEnglishIsRomanized() {
        val lyrics = """
            Twinkle twinkle little star
            How I wonder what you are
            Up above the world so high
            Like a diamond in the sky
        """.trimIndent()
        assertTrue(TextUtil.isTextRomanized(lyrics))
    }

    @Test
    fun testSpanishIsRomanized() {
        val lyrics = """
            Despacito
            Quiero respirar tu cuello despacito
            Deja que te diga cosas al oído
            Para que te acuerdes si no estás conmigo
        """.trimIndent()
        assertTrue(TextUtil.isTextRomanized(lyrics))
    }

    @Test
    fun testRomajiIsRomanized() {
        val lyrics = """
            Oshiete oshiete yo sono shikumi wo
            Boku no naka ni dare ga iru no?
            Kowareta kowareta yo kono sekai de
            Kimi ga warau nanimo miezu ni
        """.trimIndent()
        assertTrue(TextUtil.isTextRomanized(lyrics))
    }

    @Test
    fun testJapaneseKanjiIsNotRomanized() {
        val lyrics = """
            教えて 教えてよ その仕組みを
            僕の中に 誰がいるの？
            壊れた 壊れたよ この世界で
            君が笑う 何も見えずに
        """.trimIndent()
        assertFalse(TextUtil.isTextRomanized(lyrics))
    }

    @Test
    fun testHindiDevanagariIsNotRomanized() {
        val lyrics = """
            तुझ में रब् दिखता है यारा मैं क्या करूँ
            तुझ में रब् दिखता है यारा मैं क्या करूँ
            सजदे सर झुकता है यारा मैं क्या करूँ
        """.trimIndent()
        assertFalse(TextUtil.isTextRomanized(lyrics))
    }

    @Test
    fun testNullOrEmptyIsRomanized() {
        assertTrue(TextUtil.isTextRomanized(null))
        assertTrue(TextUtil.isTextRomanized(""))
        assertTrue(TextUtil.isTextRomanized("   "))
    }
}
