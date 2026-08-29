package io.github.natnaelgetachewyirga.anagram.cli

import io.github.natnaelgetachewyirga.anagram.application.AnagramService
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnagramCliTest {
    @Test
    fun `one session supports both assignment features`() {
        val result = runCli("1\nlisten\nsilent\n2\ntinsel\n0\n")

        assertContains(result.output, "Anagrams: \"listen\" and \"silent\"")
        assertContains(result.output, "Anagrams of \"tinsel\": [\"listen\", \"silent\"]")
    }

    @Test
    fun `negative comparisons explain the reason`() {
        assertContains(
            runCli("1\nListen\nlisten\n0\n").output,
            "Not anagrams: \"Listen\" and \"listen\" are the same word.",
        )
        assertContains(
            runCli("1\nlisten\nbanana\n0\n").output,
            "Not anagrams: \"listen\" and \"banana\" do not use the same letters.",
        )
    }

    @Test
    fun `search prints an empty list when there are no matches`() {
        assertContains(runCli("2\nhello\n0\n").output, "Anagrams of \"hello\": []")
    }

    @Test
    fun `invalid text is reported without ending the session`() {
        val result = runCli("1\nlisten\n12345\n1\nlisten\nsilent\n2\n!!!\n0\n")

        assertContains(result.output, "Cannot compare: \"12345\" contains no letters.")
        assertContains(result.output, "Anagrams: \"listen\" and \"silent\"")
        assertContains(result.output, "Cannot search: \"!!!\" contains no letters.")
    }

    @Test
    fun `unknown and blank commands leave the session usable`() {
        val result = runCli("\ninvalid\n0\n")

        assertEquals(3, result.output.occurrencesOf(PROMPT))
        assertContains(result.output, "Unknown command \"invalid\". Enter 1, 2 or 0.")
    }

    @Test
    fun `text is preserved exactly as entered`() {
        val result = runCli("1\n  Listen  \nsilent\n0\n")

        assertContains(result.output, "\"  Listen  \" and \"silent\"")
        assertEquals(listOf("  Listen  ", "silent"), result.service.findPreviouslyEnteredAnagrams("tinsel"))
    }

    @Test
    fun `end of input stops the session at every prompt`() {
        assertTrue(runCli("").output.endsWith(PROMPT))
        assertTrue(runCli("1\n").output.endsWith("  First text:  "))
        assertTrue(runCli("1\nlisten\n").output.endsWith("  Second text: "))
        assertTrue(runCli("2\n").output.endsWith("  Text to find: "))
    }

    @Test
    fun `an incomplete comparison changes no history`() {
        assertEquals(emptyList(), runCli("1\nlisten\n").service.findPreviouslyEnteredAnagrams("silent"))
    }

    @Test
    fun `exit ignores later input`() {
        val result = runCli("0\n1\nlisten\nsilent\n")

        assertFalse(result.output.contains("First text"))
        assertEquals(emptyList(), result.service.findPreviouslyEnteredAnagrams("silent"))
    }

    @Test
    fun `each match is quoted so a text containing a comma stays readable`() {
        val result = runCli("1\ncb, a\nabc\n2\nbca\n0\n")

        assertContains(result.output, """Anagrams of "bca": ["cb, a", "abc"]""")
    }

    @Test
    fun `a long text is shortened where it is echoed back`() {
        val result = runCli("1\n${"a".repeat(200)}\nb\n0\n")

        assertContains(result.output, "\"${"a".repeat(60)}…\"")
        assertFalse(result.output.contains("a".repeat(61)))
    }

    private fun runCli(input: String): CliRunResult {
        val service = AnagramService()
        val output = StringWriter()

        AnagramCli(
            service = service,
            input = BufferedReader(StringReader(input)),
            output = PrintWriter(output, true),
        ).run()

        return CliRunResult(service, output.toString().replace(System.lineSeparator(), "\n"))
    }

    private fun String.occurrencesOf(text: String): Int = split(text).size - 1

    private class CliRunResult(
        val service: AnagramService,
        val output: String,
    )

    private companion object {
        const val PROMPT = "\n> "
    }
}
