package io.github.natnaelgetachewyirga.anagram.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class AnagramSignatureTest {
    @ParameterizedTest(name = "{0} / {1}")
    @MethodSource("equalSignatureCases")
    fun `signature equality ignores order but preserves every letter count`(
        first: String,
        second: String,
    ) {
        assertEquals(NormalizedText.from(first).signature, NormalizedText.from(second).signature)
    }

    @ParameterizedTest(name = "{0} / {1}")
    @MethodSource("differentSignatureCases")
    fun `different letter multisets have different signatures`(
        first: String,
        second: String,
    ) {
        assertNotEquals(NormalizedText.from(first).signature, NormalizedText.from(second).signature)
    }

    @Test
    fun `a signature is not equal to values of other types`() {
        assertFalse(NormalizedText.from("listen").signature.equals("listen"))
    }

    private companion object {
        @JvmStatic
        fun equalSignatureCases(): List<Arguments> =
            listOf(
                Arguments.of("listen", "silent"),
                Arguments.of("aabbcc", "cbacba"),
                Arguments.of("Dormitory", "Dirty room!! 123"),
                Arguments.of("ፍቅር", "ቅርፍ"),
                Arguments.of("𐐀b", "B𐐨"),
            )

        @JvmStatic
        fun differentSignatureCases(): List<Arguments> =
            listOf(
                Arguments.of("aab", "abb"),
                Arguments.of("resume", "résumé"),
                Arguments.of("áb", "ab́"),
                Arguments.of("ß", "ss"),
                Arguments.of("ﬁ", "fi"),
            )
    }
}
