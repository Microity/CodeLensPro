package com.codelens.pro

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FoldAwareLineMapperTest {
    @Test
    fun `identity mapping keeps all lines`() {
        val mapping = FoldAwareLineMapper.build(4, emptyList())

        assertEquals(4, mapping.visualLineCount)
        assertContentEquals(intArrayOf(0, 1, 2, 3), mapping.documentLineToVisualLine)
        assertContentEquals(intArrayOf(0, 1, 2, 3), mapping.visualLineToDocumentLine)
        assertFalse(mapping.isFoldPlaceholderVisualLine(1))
    }

    @Test
    fun `multi line fold compresses hidden lines into start visual line`() {
        val mapping = FoldAwareLineMapper.build(6, listOf(FoldedLineRange(1, 3)))

        assertEquals(4, mapping.visualLineCount)
        assertContentEquals(intArrayOf(0, 1, 1, 1, 2, 3), mapping.documentLineToVisualLine)
        assertContentEquals(intArrayOf(0, 1, 4, 5), mapping.visualLineToDocumentLine)
        assertTrue(mapping.isFoldPlaceholderVisualLine(1))
    }

    @Test
    fun `same line fold does not become placeholder`() {
        val mapping = FoldAwareLineMapper.build(3, listOf(FoldedLineRange(1, 1)))

        assertEquals(3, mapping.visualLineCount)
        assertContentEquals(intArrayOf(0, 1, 2), mapping.documentLineToVisualLine)
        assertFalse(mapping.isFoldPlaceholderVisualLine(1))
    }

    @Test
    fun `overlapping folds merge into one compressed range`() {
        val mapping = FoldAwareLineMapper.build(8, listOf(FoldedLineRange(1, 4), FoldedLineRange(2, 6)))

        assertEquals(3, mapping.visualLineCount)
        assertContentEquals(intArrayOf(0, 1, 1, 1, 1, 1, 1, 2), mapping.documentLineToVisualLine)
        assertContentEquals(intArrayOf(0, 1, 7), mapping.visualLineToDocumentLine)
        assertTrue(mapping.isFoldPlaceholderVisualLine(1))
    }

    @Test
    fun `line lookup clamps out of range input`() {
        val mapping = FoldAwareLineMapper.build(3, emptyList())

        assertEquals(0, mapping.visualLineForDocumentLine(-10))
        assertEquals(2, mapping.visualLineForDocumentLine(20))
        assertEquals(0, mapping.documentLineForVisualLine(-10))
        assertEquals(2, mapping.documentLineForVisualLine(20))
    }
}
