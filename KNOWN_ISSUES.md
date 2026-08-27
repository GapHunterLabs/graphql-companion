# Known issues log — GraphQL Companion

Real bugs found during development, with root cause and fix. Not a TODO
list.

## Round 1 (2026-08-03) — KDoc example glob pattern opened a nested comment, swallowing the rest of the file

**Symptom:** `./gradlew test` failed at `compileKotlin` with three
errors: `Unresolved reference 'matchesGlob'` at the call site, `Missing
'}'` mid-file, and `Unclosed comment` pointing at the very last line of
`SchemaDiscovery.kt` — none of them anywhere near the actual mistake.

**Root cause:** a known Kotlin comment-nesting gotcha, independently
rediscovered here — Kotlin
block comments (`/* */`) **nest**, unlike Java/C. A KDoc comment
(`/** ... */`) documenting `matchesGlob`'s glob-matching behavior
included an example pattern containing a literal star-slash sequence
(from an escaping attempt using HTML entities that Kotlin's compiler
never interprets — `&#47;` is not a slash to the Kotlin lexer, it's five
literal characters, and one attempted substitution actually left a real
`/*` in the source). That opened a *nested* comment inside the outer
KDoc block; the KDoc's own closing `*/` only closed the innermost nested
comment, leaving the outer one open all the way to true EOF.

**Fix:** rewrote the comment as a line-comment (`//`) block instead of a
KDoc block, and described the glob-matching rule in plain English instead
of embedding a literal example pattern containing star-slash sequences at
all. A secondary, unrelated copy-paste duplication (the function
signature `private fun matchesGlob(...)` appeared twice in a row) was
introduced while fixing the comment and caught in the same recompile —
worth noting only because it's exactly the kind of second bug an editor
tool can silently introduce while fixing a first one, and is why
re-running the full test suite after any fix — not just eyeballing the
diff — is the actual verification step, not optional.

**Verified:** `./gradlew test` green (3/3 test files), including the
`toleratesLargeSchemaFileWithoutPathologicalSlowdown` assertion that
exercises the same file this bug was found in (transitively, via
`SchemaDiscovery`'s own test suite).

**Lesson (a second real occurrence of the same known gotcha):** never
let a literal `/*` or `*/` substring appear
inside a Kotlin comment, including inside an "escaped" or
entity-encoded example — Kotlin's lexer processes comment delimiters
before any string/entity semantics could possibly apply, so no
escaping scheme invented for a *string literal* protects a *comment*.
If an example needs to show characters that could form `/*`/`*/`, use a
line comment (`//`) instead of a block comment, since line comments have
no closing delimiter to accidentally trigger.
