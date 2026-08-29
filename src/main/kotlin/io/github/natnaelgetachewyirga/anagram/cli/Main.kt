package io.github.natnaelgetachewyirga.anagram.cli

import io.github.natnaelgetachewyirga.anagram.application.AnagramService
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

/** Starts the interactive session on standard input and output, reading and writing UTF-8. */
public fun main(): Unit =
    AnagramCli(
        service = AnagramService(),
        input = System.`in`.bufferedReader(StandardCharsets.UTF_8),
        output = PrintWriter(System.out.writer(StandardCharsets.UTF_8), true),
    ).run()
