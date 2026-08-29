package io.github.natnaelgetachewyirga.anagram.application

import io.github.natnaelgetachewyirga.anagram.domain.ComparisonResult.ANAGRAMS
import io.github.natnaelgetachewyirga.anagram.domain.ComparisonResult.DIFFERENT_LETTERS
import io.github.natnaelgetachewyirga.anagram.domain.ComparisonResult.SAME_WORD
import io.github.natnaelgetachewyirga.anagram.domain.InvalidTextException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnagramServiceTest {
    private val service = AnagramService()

    @Test
    fun `the worked example produces the specified results`() {
        val a = "listen"
        val b = "silent"
        val c = "banana"
        val d = "enlist"

        service.checkAndRemember(a, b)
        service.checkAndRemember(a, c)
        service.checkAndRemember(a, d)

        assertEquals(listOf(b, d), service.findPreviouslyEnteredAnagrams(a))
        assertEquals(listOf(a, d), service.findPreviouslyEnteredAnagrams(b))
        assertEquals(emptyList(), service.findPreviouslyEnteredAnagrams(c))
    }

    @Test
    fun `comparison distinguishes anagrams same words and different letters`() {
        assertEquals(ANAGRAMS, service.checkAndRemember("listen", "silent"))
        assertEquals(SAME_WORD, service.checkAndRemember("Listen!!", " listen "))
        assertEquals(DIFFERENT_LETTERS, service.checkAndRemember("listen", "banana"))
    }

    @Test
    fun `both valid inputs are remembered after a negative comparison`() {
        service.checkAndRemember("listen", "banana")

        assertEquals(listOf("listen"), service.findPreviouslyEnteredAnagrams("silent"))
        assertEquals(listOf("banana"), service.findPreviouslyEnteredAnagrams("aaabnn"))
    }

    @Test
    fun `invalid comparison input changes no history`() {
        assertThrows<InvalidTextException> { service.checkAndRemember("listen", "123") }
        assertThrows<InvalidTextException> { service.checkAndRemember("!!!", "silent") }

        assertEquals(emptyList(), service.findPreviouslyEnteredAnagrams("tinsel"))
    }

    @Test
    fun `texts differing only in digits are the same word`() {
        assertEquals(SAME_WORD, service.checkAndRemember("abc1", "abc2"))
    }

    @Test
    fun `signature groups are independent of each other`() {
        service.checkAndRemember("listen", "banana")
        service.checkAndRemember("enlist", "nabana")
        service.checkAndRemember("silent", "abanan")

        assertEquals(listOf("listen", "enlist", "silent"), service.findPreviouslyEnteredAnagrams("tinsel"))
        assertEquals(listOf("banana", "nabana", "abanan"), service.findPreviouslyEnteredAnagrams("aaabnn"))
    }

    @Test
    fun `history deduplicates exact text and preserves first-seen order`() {
        service.checkAndRemember("listen", "silent")
        service.checkAndRemember("enlist", "listen")
        service.checkAndRemember("silent", "inlets")

        assertEquals(
            listOf("listen", "silent", "enlist", "inlets"),
            service.findPreviouslyEnteredAnagrams("tinsel"),
        )
    }

    @Test
    fun `search excludes normalized spellings of its query`() {
        service.checkAndRemember("Listen", "listen!!")
        service.checkAndRemember("silent", "enlist")

        assertEquals(listOf("silent", "enlist"), service.findPreviouslyEnteredAnagrams("LISTEN"))
    }

    @Test
    fun `search does not add its query to history`() {
        service.checkAndRemember("listen", "banana")

        service.findPreviouslyEnteredAnagrams("tinsel")

        assertEquals(listOf("listen"), service.findPreviouslyEnteredAnagrams("silent"))
    }

    @Test
    fun `invalid search leaves existing history unchanged`() {
        service.checkAndRemember("listen", "silent")

        assertThrows<InvalidTextException> { service.findPreviouslyEnteredAnagrams("😀") }

        assertEquals(listOf("listen", "silent"), service.findPreviouslyEnteredAnagrams("tinsel"))
    }

    @Test
    fun `search returns an immutable snapshot`() {
        service.checkAndRemember("listen", "silent")
        val snapshot = service.findPreviouslyEnteredAnagrams("tinsel")

        assertFailsWith<UnsupportedOperationException> {
            (snapshot as MutableList<String>).add("enlist")
        }
        service.checkAndRemember("enlist", "inlets")
        assertEquals(listOf("listen", "silent"), snapshot)
    }

    @Test
    fun `service instances do not share history`() {
        service.checkAndRemember("listen", "silent")

        assertEquals(emptyList(), AnagramService().findPreviouslyEnteredAnagrams("tinsel"))
    }
}
