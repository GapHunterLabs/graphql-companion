# Demo content

- `services/users-service/schema/users.graphqls`,
  `services/orders-service/schema/orders.graphqls`,
  `shared/schema/common.graphqls`, plus `.graphqlconfig` declaring two
  named projects (`users`, `orders`) each spanning its own service schema
  plus the shared one — a realistic multi-schema layout, the direct
  demonstration of the fix for "does not work with multiple schemas."
- `large-schema/generated-large.graphqls` — a 4200-line, syntactically
  valid (but repetitive/generated) schema file, used to confirm typing
  latency stays acceptable during manual `runIde` inspection, and as the
  basis for `GraphqlLexerTest`'s automated latency assertion (tokenizing
  it must complete in well under 1 second).
