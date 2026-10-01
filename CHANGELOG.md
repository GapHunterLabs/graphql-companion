<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# GraphQL Companion Changelog

## [Unreleased]

## [0.1.2]

### Fixed

- Settings > Tools > GraphQL Companion always said "No schema files
  detected yet in this project": the schema discovery was never run on
  the project. The page now lists the project's schema groups -- the
  `projects` of a `.graphqlconfig` at the project root, or groups by
  directory -- computed in the background when it opens.
- A single `*` in a `.graphqlconfig` schema glob matched across
  directories; it now stays within one path segment (`**` crosses them).

## [0.1.1]

### Fixed

- Marketplace listing icon not rendering (showed a broken "plugin icon"
  placeholder) — replaced with the same icon already proven to render
  correctly on other Gap Hunter Labs listings.

## [0.1.0]

### Added

- Fast, single-pass lexer-based syntax highlighting for
  `.graphql`/`.graphqls` files that stays fast on large (4000+ line)
  schema files.
- Multi-schema discovery: groups schema files by directory automatically
  with zero configuration, or by explicit `.graphqlconfig` `projects`
  entries when present.
- Read-only Settings page showing detected schema groups.

### Known gaps

- No query validation against a schema, no autocomplete, no
  go-to-definition — deferred, not attempted in v0.1 (a full language
  server's worth of scope, out of reach for a one-night build).

[Unreleased]: https://github.com/GapHunterLabs/graphql-companion/compare/0.1.2...HEAD
[0.1.2]: https://github.com/GapHunterLabs/graphql-companion/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/GapHunterLabs/graphql-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/graphql-companion/commits/0.1.0
