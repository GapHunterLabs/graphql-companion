# GraphQL Companion

IntelliJ-family plugin. Fast syntax highlighting for
`.graphql`/`.graphqls` files, and multi-schema discovery that works out
of the box — with or without a `.graphqlconfig` file.

## Why it exists

Born from real evidence in JetBrains's own official GraphQL plugin
(millions of downloads), not assumptions:

- "Basically does not work in projects with multiple schemas. The
  documentation for setting up scopes is terrible and it straight up
  does not work at all." — multi-schema/multi-endpoint support has been
  an open, unresolved request since 2016.
- Performance degrades to roughly 1 character per 1.2-1.4 seconds, with
  no syntax highlighting at all, on large (3000+ line) `.graphqls`
  files.
- A recent regression: schema discovery reported broken after upgrading
  to a newer IDE version.

## Why built this way

- **A purpose-built, single-pass lexer, not a general-purpose
  incremental parser.** GraphQL's SDL grammar is small and stable
  (keywords, punctuation, strings, block strings, numbers, names) —
  scanning it with a manual, backtracking-free scanner is orders of
  magnitude cheaper per keystroke than a parser struggling on scope
  resolution. This is the direct, structural fix for the cited
  performance regression, not a bolt-on optimization — verified by an
  automated test that tokenizes a real 4200-line generated schema file
  and asserts it completes in well under a second (see
  `GraphqlLexerTest`'s own latency assertion).
- **Multi-schema discovery that works with zero configuration.** Schema
  files are grouped by directory automatically (the segment before a
  `schema/`/`graphql/` folder, or the first path segment otherwise) — a
  project doesn't need a working `.graphqlconfig` to get correct
  multi-schema behavior, unlike the cited "does not work with multiple
  schemas" complaint. When a `.graphqlconfig` with a `projects` block
  IS present, it takes priority and is parsed directly (best-effort, no
  new JSON-parsing dependency — same call already made elsewhere in this
  workspace).
- **Detected groups are visible in Settings**, not a black box — the
  direct fix for "the documentation for setting up scopes is terrible":
  users can see what was actually detected without reading any docs at
  all.

## Usage

Open any `.graphql`/`.graphqls` file — syntax highlighting is automatic.
Settings > Tools > GraphQL Companion shows the schema groups detected in
the current project.

## Enterprise / Team Licensing

Need enterprise features, schema validation, or team licensing? Contact
us at **gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

`demo/` has a realistic multi-service schema layout (see `demo/README.md`)
plus a large generated schema file used both for manual inspection and
as the automated lexer-latency test's fixture.

## License

Apache-2.0. See `LICENSE`.
