package com.codelens.pro

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GlyphKindTest {
    @Test
    fun `whitespace and control characters are skipped`() {
        assertNull(GlyphKind.from(' '))
        assertNull(GlyphKind.from('\t'))
        assertNull(GlyphKind.from('\n'))
    }

    @Test
    fun `letters and digits use letter glyph`() {
        assertEquals(GlyphKind.LETTER_OR_DIGIT, GlyphKind.from('a'))
        assertEquals(GlyphKind.LETTER_OR_DIGIT, GlyphKind.from('Z'))
        assertEquals(GlyphKind.LETTER_OR_DIGIT, GlyphKind.from('7'))
    }

    @Test
    fun `syntax characters use specific glyph kinds`() {
        assertEquals(GlyphKind.BRACKET, GlyphKind.from('{'))
        assertEquals(GlyphKind.BRACKET, GlyphKind.from(')'))
        assertEquals(GlyphKind.QUOTE, GlyphKind.from('"'))
        assertEquals(GlyphKind.QUOTE, GlyphKind.from('\''))
        assertEquals(GlyphKind.PUNCTUATION, GlyphKind.from('.'))
        assertEquals(GlyphKind.PUNCTUATION, GlyphKind.from(','))
    }

    @Test
    fun `unknown visible characters use other glyph`() {
        assertEquals(GlyphKind.OTHER, GlyphKind.from('€'))
    }
}
