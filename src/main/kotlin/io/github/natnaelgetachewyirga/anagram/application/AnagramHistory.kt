package io.github.natnaelgetachewyirga.anagram.application

import io.github.natnaelgetachewyirga.anagram.domain.AnagramSignature
import io.github.natnaelgetachewyirga.anagram.domain.NormalizedText

/**
 * In-memory store of Feature 1 inputs, grouped by [AnagramSignature].
 *
 * Each group maps an exact input to its canonical form: O(1) duplicate suppression and first-seen
 * iteration order from one structure, plus the one value a search needs to skip the query's own
 * spellings. The signature is the group key, so it is not stored again per entry.
 */
internal class AnagramHistory {
    private val groupsBySignature = HashMap<AnagramSignature, LinkedHashMap<String, String>>()

    /** Stores [original] under its signature. An exact string already present is left as it was. */
    fun remember(
        original: String,
        normalized: NormalizedText,
    ) {
        groupsBySignature
            .getOrPut(normalized.signature) { LinkedHashMap() }
            .putIfAbsent(original, normalized.canonical)
    }

    /**
     * Returns the stored inputs that are anagrams of [query], in first-seen order.
     *
     * Only the matching group is examined; stored spellings of the query itself are left out.
     */
    fun anagramsOf(query: NormalizedText): List<String> {
        val group = groupsBySignature[query.signature] ?: return emptyList()

        return buildList {
            for ((original, canonical) in group) {
                if (canonical != query.canonical) {
                    add(original)
                }
            }
        }
    }
}
