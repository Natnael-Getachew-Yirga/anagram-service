package io.github.natnaelgetachewyirga.anagram.domain

/**
 * The multiset of letters in a [NormalizedText]: which letters occur, and how often.
 *
 * Equal signatures mean two texts use exactly the same letters, in any order, which is what
 * groups anagrams together in history.
 */
internal class AnagramSignature private constructor(
    private val letterCounts: Map<String, Int>,
) {
    // A map recomputes its hash on every call, and this type exists to be a map key.
    private val hash: Int = letterCounts.hashCode()

    override fun equals(other: Any?): Boolean = other is AnagramSignature && letterCounts == other.letterCounts

    override fun hashCode(): Int = hash

    override fun toString(): String = letterCounts.toString()

    companion object {
        /** Counts each entry of [letters]. Counting is O(n); sorting the letters instead would be O(n log n). */
        fun from(letters: List<String>): AnagramSignature = AnagramSignature(letters.groupingBy { it }.eachCount())
    }
}
