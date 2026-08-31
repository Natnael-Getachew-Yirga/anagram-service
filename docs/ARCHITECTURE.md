# Design

## Task

- Interactive Java 8+ or Kotlin program, two features.
- Feature 1: compare two texts, report whether they are anagrams.
- Feature 2: given a text, return all anagrams previously entered through Feature 1.
- History lasts one program run. Source must be public.
- Priorities are free to choose; this solution favours correct Unicode handling and readability over raw speed.

## Definition

The task links the [English Wikipedia definition](https://en.wikipedia.org/wiki/Anagram): a *different* word or
phrase made by rearranging all letters of the original. After normalization that is two checks:

| Check | Meaning |
|-------|---------|
| Letter counts equal | Every retained letter occurs the same number of times |
| Letter order differs | A word is not its own anagram |

| First text | Second text | Result              | Reason                                           |
|------------|-------------|---------------------|--------------------------------------------------|
| `listen`   | `silent`    | `ANAGRAMS`          | Same letters and counts, different order         |
| `listen`   | `listen`    | `SAME_WORD`         | Same letter sequence                             |
| `Listen!!` | ` listen `  | `SAME_WORD`         | Ignored content does not create a different word |
| `aab`      | `abb`       | `DIFFERENT_LETTERS` | Repeated-letter counts differ                    |

## What the worked example pins down

`f1(A, B)`, `f1(A, C)`, `f1(A, D)` with `A`, `B`, `D` anagrams and `C` not, then
`f2(A) = [B, D]`, `f2(B) = [A, D]`, `f2(C) = []`:

- both Feature 1 arguments enter history, second arguments included;
- repeated `A` values appear once, so history deduplicates;
- a query is excluded from its own result;
- results follow first-seen order.

The example cannot show whether `C` was stored, because a query excludes itself either way.
The prose settles it: history holds all Feature 1 inputs, so `C` is stored. Feature 2 searches
that history and does not add to it.

## Decisions the task leaves open

| Question                    | Decision                                                         | Example or reason                                                            |
|-----------------------------|------------------------------------------------------------------|------------------------------------------------------------------------------|
| Capitalization              | Ignore case, locale-independent one-to-one folding               | `Listen` matches `silent`; behavior does not depend on the host locale       |
| Whitespace and punctuation  | Ignore                                                           | `Dormitory` matches `Dirty room!!`                                           |
| Digits, symbols, emoji      | Ignore; they are not letters                                     | `abc1` and `abc2` are the same word                                          |
| Unicode spelling            | NFC, so canonically equivalent spellings agree                   | composed `é` equals decomposed `e` + accent                                  |
| Counted unit                | Grapheme cluster containing at least one letter                  | combining marks stay on their base letter                                    |
| Diacritics                  | Keep meaningful                                                  | `resume` differs from `résumé`                                               |
| Compatibility characters    | No NFKC expansion                                                | `ﬁ` differs from `fi`; `ß` differs from `ss`                                 |
| Input with no letters       | Reject with `InvalidTextException`                               | blank, `123`, punctuation-only, emoji-only                                   |
| Same word                   | Equal letter sequences are not anagrams                          | the definition says "different"                                              |
| Duplicate input             | Equal original `String` values are one entry                     | `Listen` and `listen` stay separate stored inputs                            |
| Query exclusion             | Exclude every stored value equal after normalization             | searching `LISTEN` excludes stored `Listen` and `listen!!`                   |
| One input invalid           | Store neither                                                    | avoids a partial history update                                              |
| Returned list               | Immutable snapshot                                               | callers cannot mutate history or see later updates through an old result     |
| Long input                  | Shorten where echoed, store in full                              | a pasted paragraph should not flood the terminal                             |

Non-Latin and supplementary-plane letters work. No language-specific sorting or transliteration.

## Structure

```text
CLI -> Application -> Domain
```

| Package       | Owns                                             | Types                                                                          |
|---------------|--------------------------------------------------|--------------------------------------------------------------------------------|
| `domain`      | Normalization, and what an anagram is            | `NormalizedText`, `AnagramSignature`, `ComparisonResult`, `InvalidTextException` |
| `application` | Both features, process-local state               | `AnagramService`, `AnagramHistory`                                             |
| `cli`         | Exact input, rendering, wiring                   | `AnagramCli`, `Main.kt`                                                        |

- The rule lives in `NormalizedText.relationTo`; `AnagramService` only validates, stores, returns.
- Public: `AnagramService`, `ComparisonResult`, `InvalidTextException`, `main`. Everything else internal.
- No repository interface: the task needs only one process-local implementation.

## Data structures

Two representations of a text, because two questions are asked of it:

| Type                       | Keeps  | Answers                        |
|----------------------------|--------|--------------------------------|
| `NormalizedText.canonical` | Order  | Is this the same word?         |
| `AnagramSignature`         | Counts | Do these use the same letters? |

| Signature equal? | Canonical equal? | Result              |
|------------------|------------------|---------------------|
| No               | Either           | `DIFFERENT_LETTERS` |
| Yes              | Yes              | `SAME_WORD`         |
| Yes              | No               | `ANAGRAMS`          |

`ComparisonResult` is an enum, not a Boolean, so the CLI can say why two texts are not anagrams.

History is:

```text
HashMap<AnagramSignature, LinkedHashMap<String, String>>
```

- Outer: signature to group. A plain `HashMap`, because nothing iterates it.
- Inner: exact input to canonical form. A `LinkedHashMap`, because its order is visible in the output.

The inner structure has to return the exact input as typed, drop exact duplicates in O(1), iterate in
first-seen order, and know each entry's canonical form so a search can skip the query's own spellings.
`LinkedHashMap` is the only single structure that does all four: `HashMap` and `TreeMap` do not keep
first-seen order, `LinkedHashSet` cannot hold the canonical form, and a list turns the duplicate check
into a linear scan.

The stored value is the canonical string rather than the whole `NormalizedText`: every entry in a group
already shares that group's signature, so storing it per entry would repeat the key.

## Normalization

Terms are the Unicode ones: [normalization forms (UAX #15)](https://unicode.org/reports/tr15/) and
[grapheme clusters (UAX #29)](https://unicode.org/reports/tr29/).

1. NFC, so canonically equivalent spellings agree.
2. Drop format characters such as zero-width joiners, which would otherwise move grapheme boundaries.
3. Case-fold every code point with the simple, locale-independent mappings.
4. NFC again, because folding can expose a newly composable sequence.
5. Split into grapheme clusters with `\X`, keep those containing a letter.
6. Reject if nothing remains.

Steps 3 and 4 run over the whole string, not per cluster: simple mappings are one-to-one and never
turn a base character into a combining mark, so cluster boundaries do not move.

- NFC rather than accent stripping: canonically equivalent text is equal, diacritics stay meaningful.
- NFC rather than NFKC: `ﬁ` is not silently expanded into `fi`.
- Simple mappings stay one-to-one: one letter never becomes several, so `ß` stays `ß`.
- Uppercase before lowercase: unifies positional forms such as Greek final and medial sigma.
- Letters join with a space in the canonical form: a space is always its own cluster and never sits
  inside a letter, so the joined form is unambiguous.

## Performance

`n` is the length of an input, `b` the number of texts already stored under the same signature, and
`k` the number of matches returned.

| Operation                                | Expected time                                  |
|------------------------------------------|------------------------------------------------|
| Normalize a text and build its signature | O(n)                                           |
| Feature 1                                | O(n) over the two inputs                       |
| Feature 2                                | O(n) to normalize, then O(b + k) over one group |

Counting graphemes is O(n); sorting them into a signature would be O(n log n). Grouping history by
signature is what keeps Feature 2 from scanning unrelated inputs. `AnagramSignature` caches its hash
code because it exists to be a map key.

## Rejected alternatives

| Alternative                     | Why not                                                                        |
|---------------------------------|---------------------------------------------------------------------------------|
| Sorted canonical signature      | O(n log n) rather than O(n), and sorting clusters needs a total order where counting needs only equality |
| Fixed ASCII frequency array     | Does not handle non-ASCII letters                                               |
| Flat history list               | Every query would scan unrelated inputs                                         |
| Group history by canonical form | Skips same-word entries quickly, but loses interleaved first-seen order without another index |
| Persist history                 | The task says one execution                                                     |
| Repository or policy interfaces | History has one implementation                                                  |
| Spring Boot or Ktor             | The task asks for an interactive program, not an HTTP service                   |
| Coroutines or parallel streams  | Work is small, CPU-bound and sequential; coordination would cost more than it saves |
| Deployment workflow             | Nothing to deploy; the repository needs CI only                                 |

## Tests

Tests cover behavior at each package boundary without inspecting private fields.

- Domain: letter counts, order, case folding, ignored content, NFC, diacritics, non-Latin text, invalid input.
- Application: the worked example verbatim, every outcome, atomic validation, deduplication, ordering
  within and across groups, query exclusion, snapshot immutability, instance isolation.
- CLI: both features, unknown commands, invalid text, exact input preservation, truncation, exit, end of input.

## Limits

- One instance is not thread-safe.
- History is unbounded until the process exits.
- No persistence, network API, authentication or deployment target.

If that changes:

- a size policy belongs inside `AnagramHistory`;
- synchronization belongs at the application-state boundary, and needs more than a `ConcurrentHashMap`,
  because `getOrPut` is a non-atomic get-then-put;
- an HTTP or desktop adapter can call the same `AnagramService`;
- a persistence interface is justified once a persistent implementation exists.
