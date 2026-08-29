package io.github.natnaelgetachewyirga.anagram.domain

/** How two valid texts relate under the Wikipedia anagram definition. */
public enum class ComparisonResult {
    /** The same letters in a different order. */
    ANAGRAMS,

    /** The same letters in the same order: a word is not an anagram of itself. */
    SAME_WORD,

    /** Different letters, or different counts of the same letters. */
    DIFFERENT_LETTERS,
}
