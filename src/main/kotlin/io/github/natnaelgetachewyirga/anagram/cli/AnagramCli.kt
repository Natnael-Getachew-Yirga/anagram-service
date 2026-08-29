package io.github.natnaelgetachewyirga.anagram.cli

import io.github.natnaelgetachewyirga.anagram.application.AnagramService
import io.github.natnaelgetachewyirga.anagram.domain.ComparisonResult
import io.github.natnaelgetachewyirga.anagram.domain.InvalidTextException
import java.io.BufferedReader
import java.io.PrintWriter

/**
 * Interactive menu over [AnagramService].
 *
 * Reads commands and texts from [input] and writes results to [output].
 */
internal class AnagramCli(
    private val service: AnagramService,
    private val input: BufferedReader,
    private val output: PrintWriter,
) {
    /** Runs the menu until the user exits with `0` or the input ends. */
    fun run() {
        output.println(MENU)

        while (true) {
            output.println()
            if (executeCommand(prompt("> ")) == Session.END) {
                return
            }
        }
    }

    /** Dispatches one menu command and reports whether the session should continue. */
    private fun executeCommand(line: String?): Session {
        val command = (line ?: return Session.END).trim()

        return when (command) {
            "" -> Session.CONTINUE
            "0" -> Session.END
            "1" -> compareTexts()
            "2" -> findAnagrams()
            else -> reportUnknownCommand(command)
        }
    }

    /** Feature 1: reads two texts, compares them and records both. */
    private fun compareTexts(): Session {
        val first = prompt("  First text:  ") ?: return Session.END
        val second = prompt("  Second text: ") ?: return Session.END

        runHandlingInvalidText("compare") {
            output.println(formatComparisonResult(first, second, service.checkAndRemember(first, second)))
        }
        return Session.CONTINUE
    }

    /** Feature 2: reads a query and prints the matching earlier inputs. */
    private fun findAnagrams(): Session {
        val query = prompt("  Text to find: ") ?: return Session.END

        runHandlingInvalidText("search") {
            output.println(formatMatches(query, service.findPreviouslyEnteredAnagrams(query)))
        }
        return Session.CONTINUE
    }

    /** Reports an unrecognized menu option without ending the session. */
    private fun reportUnknownCommand(command: String): Session {
        output.println("Unknown command ${command.quoted()}. Enter 1, 2 or 0.")
        return Session.CONTINUE
    }

    /** Turns rejected text into a message instead of letting it end the session. */
    private fun runHandlingInvalidText(
        attempt: String,
        action: () -> Unit,
    ) {
        try {
            action()
        } catch (invalid: InvalidTextException) {
            output.println("Cannot $attempt: ${invalid.text.quoted()} contains no letters.")
        }
    }

    /** Shows [message] and reads one line, returning null at end of input. */
    private fun prompt(message: String): String? {
        output.print(message)
        output.flush()
        return input.readLine()
    }

    /** Whether the interactive session keeps running. */
    private enum class Session { CONTINUE, END }
}

private val MENU: String =
    """
    Anagram Service
      [1] Check two texts
      [2] Find previous anagrams
      [0] Exit
    """.trimIndent()

/** Long inputs are echoed in every message, so they are shortened to keep the output readable. */
private const val MAX_ECHOED_LENGTH = 60

private fun formatComparisonResult(
    first: String,
    second: String,
    result: ComparisonResult,
): String =
    when (result) {
        ComparisonResult.ANAGRAMS -> {
            "Anagrams: ${first.quoted()} and ${second.quoted()} use the same letters in a different order."
        }

        ComparisonResult.SAME_WORD -> {
            "Not anagrams: ${first.quoted()} and ${second.quoted()} are the same word."
        }

        ComparisonResult.DIFFERENT_LETTERS -> {
            "Not anagrams: ${first.quoted()} and ${second.quoted()} do not use the same letters."
        }
    }

private fun formatMatches(
    query: String,
    matches: List<String>,
): String = matches.joinToString(prefix = "Anagrams of ${query.quoted()}: [", postfix = "]") { it.quoted() }

/** Quotes a text for display, escaping quotes it contains and shortening it if it is long. */
private fun String.quoted(): String {
    val shown = if (length <= MAX_ECHOED_LENGTH) this else take(MAX_ECHOED_LENGTH) + "…"
    return "\"${shown.replace("\"", "\\\"")}\""
}
