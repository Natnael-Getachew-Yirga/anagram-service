package io.github.natnaelgetachewyirga.anagram.domain

import java.text.Normalizer
import java.util.regex.Pattern

/**
 * A text reduced to the letters that decide anagram equivalence.
 *
 * Two instances are equal when they are the same word. Equal [signature]s mean the two texts
 * use the same letters, whatever their order.
 */
internal class NormalizedText private constructor(
    /** The retained letters in reading order. */
    val canonical: String,
    /** The retained letters as an unordered multiset. */
    val signature: AnagramSignature,
) {
    /** How this text relates to [other]: the same letters reordered, the same word, or different letters. */
    fun relationTo(other: NormalizedText): ComparisonResult =
        when {
            signature != other.signature -> ComparisonResult.DIFFERENT_LETTERS
            canonical == other.canonical -> ComparisonResult.SAME_WORD
            else -> ComparisonResult.ANAGRAMS
        }

    override fun equals(other: Any?): Boolean = other is NormalizedText && canonical == other.canonical

    override fun hashCode(): Int = canonical.hashCode()

    override fun toString(): String = canonical

    internal companion object {
        /** Separates letters in [canonical]. A space is always its own cluster, so it never sits inside a letter. */
        private const val SEPARATOR = " "

        /** One extended grapheme cluster: a base character with any marks attached to it. */
        private val GRAPHEME_CLUSTER: Pattern = Pattern.compile("\\X")

        /**
         * Reduces [text] to its letters:
         *
         * 1. NFC-compose, so canonically equivalent spellings agree.
         * 2. Drop format characters, which would otherwise move grapheme cluster boundaries.
         * 3. Case-fold, then NFC again because folding can expose a newly composable sequence.
         * 4. Keep the grapheme clusters that contain a letter.
         *
         * @throws InvalidTextException if no letters remain.
         */
        fun from(text: String): NormalizedText {
            val composed = Normalizer.normalize(text, Normalizer.Form.NFC)
            val folded = foldCase(dropFormatCharacters(composed))
            val letters = lettersOf(Normalizer.normalize(folded, Normalizer.Form.NFC))

            if (letters.isEmpty()) {
                throw InvalidTextException(text)
            }
            return NormalizedText(letters.joinToString(SEPARATOR), AnagramSignature.from(letters))
        }

        /** Removes invisible format code points such as zero-width joiners. */
        private fun dropFormatCharacters(text: String): String {
            val kept = StringBuilder(text.length)
            text
                .codePoints()
                .filter { codePoint -> Character.getType(codePoint) != Character.FORMAT.toInt() }
                .forEach(kept::appendCodePoint)
            return kept.toString()
        }

        /**
         * Applies the simple, locale-independent case mappings to every code point.
         *
         * Routing through uppercase unifies positional variants such as Greek final and medial sigma.
         * The mappings stay one-to-one, so one letter never expands into several and `ß` stays `ß`.
         */
        private fun foldCase(text: String): String {
            val folded = StringBuilder(text.length)
            text.codePoints().forEach { codePoint ->
                folded.appendCodePoint(Character.toLowerCase(Character.toUpperCase(codePoint)))
            }
            return folded.toString()
        }

        /** Splits [text] into grapheme clusters and keeps the ones holding at least one letter. */
        private fun lettersOf(text: String): List<String> {
            val matcher = GRAPHEME_CLUSTER.matcher(text)
            return buildList {
                while (matcher.find()) {
                    val cluster = matcher.group()
                    if (cluster.codePoints().anyMatch(Character::isLetter)) {
                        add(cluster)
                    }
                }
            }
        }
    }
}
