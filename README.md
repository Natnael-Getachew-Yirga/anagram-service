# Anagram Service

Interactive Kotlin/JVM implementation of the two assignment features:

- compare two texts;
- find previously compared texts that are anagrams of a query;
- keep history only for the current process.

## Requirements

- JDK 21
- No local Gradle installation; use the included wrapper

## Run

```shell
./gradlew run -q
```

| Input | Action                              |
|------:|-------------------------------------|
|   `1` | Compare two texts and remember both |
|   `2` | Search Feature 1 history            |
|   `0` | Exit                                |

Build a standalone launcher:

```shell
./gradlew installDist
./build/install/anagram-service/bin/anagram-service
```

## Example

The session below is the worked example from the task, with `A = listen`, `B = silent`,
`C = banana` and `D = enlist`.

```text
Anagram Service
  [1] Check two texts
  [2] Find previous anagrams
  [0] Exit

> 1
  First text:  listen
  Second text: silent
Anagrams: "listen" and "silent" use the same letters in a different order.

> 1
  First text:  listen
  Second text: banana
Not anagrams: "listen" and "banana" do not use the same letters.

> 1
  First text:  listen
  Second text: enlist
Anagrams: "listen" and "enlist" use the same letters in a different order.

> 2
  Text to find: listen
Anagrams of "listen": ["silent", "enlist"]

> 2
  Text to find: banana
Anagrams of "banana": []

> 0
```

## Verify

```shell
./gradlew clean build --warning-mode=fail
```

This command runs:

- Kotlin compilation with warnings as errors;
- tests;
- ktlint;
- coverage verification.

## Behavior

- Following the [English Wikipedia definition](https://en.wikipedia.org/wiki/Anagram), anagrams use every normalized letter exactly once in a different order.
- A text is not an anagram of itself: two texts that normalize to the same letters in the same order are reported as the same word, not as anagrams.
- Case, whitespace, punctuation, symbols, emoji and digits are ignored. Only letters count, so `abc1` and `abc2` are the same word.
- Canonically equivalent Unicode spellings compare equally.
- Diacritics remain significant: `resume` and `résumé` differ.
- Text containing no letters is rejected.
- Feature 1 validates both inputs before storing either one.
- Feature 1 stores both valid inputs even when they are not anagrams.
- Exact repeated inputs are stored once.
- Feature 2 preserves first-seen order and never stores its query.
- Long inputs are shortened where they are echoed back; history keeps them in full.
- History is not shared between application runs.

See [ARCHITECTURE.md](docs/ARCHITECTURE.md) for requirements, assumptions and design decisions.
