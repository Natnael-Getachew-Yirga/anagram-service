package io.github.natnaelgetachewyirga.anagram.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class NormalizedTextTest {
    @ParameterizedTest(name = "{0} / {1}")
    @MethodSource("ignoredContentCases")
    fun `case and non-letter content do not change normalized text`(
        first: String,
        second: String,
    ) {
        assertEquals(NormalizedText.from(first), NormalizedText.from(second))
    }

    @ParameterizedTest(name = "{0} / {1}")
    @MethodSource("caseFoldCases")
    fun `case variants normalize to one letter`(
        variant: String,
        letter: String,
    ) {
        assertEquals(NormalizedText.from(letter), NormalizedText.from(variant))
    }

    @ParameterizedTest(name = "{0} / {1}")
    @MethodSource("canonicalEquivalenceCases")
    fun `canonically equivalent spellings normalize equally`(
        decomposed: String,
        composed: String,
    ) {
        assertEquals(NormalizedText.from(composed), NormalizedText.from(decomposed))
    }

    @ParameterizedTest(name = "[{0}]")
    @ValueSource(strings = ["", " \t\n", "!?.,", "12345", "😀🚀"])
    fun `text without letters is rejected`(text: String) {
        val rejected = assertThrows<InvalidTextException> { NormalizedText.from(text) }

        assertEquals(text, rejected.text)
    }

    @Test
    fun `normalized equality preserves letter order`() {
        val listen = NormalizedText.from("listen")
        val silent = NormalizedText.from("silent")

        assertNotEquals(listen, silent)
        assertEquals(listen.signature, silent.signature)
    }

    @Test
    fun `hash codes agree for equal texts and differ for unequal ones`() {
        assertEquals(
            NormalizedText.from("dirtyroom").hashCode(),
            NormalizedText.from("Dirty room!!").hashCode(),
        )
        // String.hashCode is specified, so this is deterministic rather than a coin flip.
        assertNotEquals(
            NormalizedText.from("listen").hashCode(),
            NormalizedText.from("silent").hashCode(),
        )
    }

    @Test
    fun `normalized text is not equal to values of other types`() {
        assertFalse(NormalizedText.from("listen").equals("listen"))
    }

    private companion object {
        @JvmStatic
        fun ignoredContentCases(): List<Arguments> =
            listOf(
                Arguments.of("Listen!! 123", "listen"),
                Arguments.of("a👨‍👩‍👧b", "ab"),
                Arguments.of("lis‍ten", "listen"),
                Arguments.of("a!\u0301b", "ab"),
            )

        @JvmStatic
        fun caseFoldCases(): List<Arguments> =
            listOf(
                Arguments.of("ς", "σ"),
                Arguments.of("µ", "μ"),
                Arguments.of("ſ", "s"),
                Arguments.of("ẞ", "ß"),
            )

        @JvmStatic
        fun canonicalEquivalenceCases(): List<Arguments> =
            listOf(
                Arguments.of("é", "é"),
                Arguments.of("H̱", "ẖ"),
            )
    }
}
