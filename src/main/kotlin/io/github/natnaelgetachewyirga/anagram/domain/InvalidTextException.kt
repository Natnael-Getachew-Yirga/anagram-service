package io.github.natnaelgetachewyirga.anagram.domain

/** Thrown when [text] holds no letters once whitespace, punctuation, digits, symbols and emoji are ignored. */
public class InvalidTextException(
    public val text: String,
) : IllegalArgumentException("Text contains no letters: \"$text\"")
