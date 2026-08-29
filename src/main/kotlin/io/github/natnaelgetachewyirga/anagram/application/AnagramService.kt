package io.github.natnaelgetachewyirga.anagram.application

import io.github.natnaelgetachewyirga.anagram.domain.ComparisonResult
import io.github.natnaelgetachewyirga.anagram.domain.InvalidTextException
import io.github.natnaelgetachewyirga.anagram.domain.NormalizedText

/**
 * The two assignment features over a history that lives exactly as long as this instance.
 *
 * Not thread-safe.
 */
public class AnagramService {
    private val history = AnagramHistory()

    /**
     * Feature 1: compares [first] with [second] and remembers both.
     *
     * Both texts are validated before either is stored, so a rejected call leaves history untouched.
     * Both are stored even when they turn out not to be anagrams.
     *
     * @throws InvalidTextException if either text contains no letters.
     */
    public fun checkAndRemember(
        first: String,
        second: String,
    ): ComparisonResult {
        val firstText = NormalizedText.from(first)
        val secondText = NormalizedText.from(second)

        history.remember(first, firstText)
        history.remember(second, secondText)

        return firstText.relationTo(secondText)
    }

    /**
     * Feature 2: returns the [checkAndRemember] inputs that are anagrams of [query], in first-seen order.
     *
     * The query is not stored, and stored spellings of the query itself are excluded.
     *
     * @throws InvalidTextException if [query] contains no letters.
     */
    public fun findPreviouslyEnteredAnagrams(query: String): List<String> =
        history.anagramsOf(NormalizedText.from(query))
}
