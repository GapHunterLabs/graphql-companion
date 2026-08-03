package dev.gaphunter.graphqlcompanion.lang

import com.intellij.psi.tree.IElementType

class GraphqlTokenType(debugName: String) : IElementType(debugName, GraphqlLanguage)

object GraphqlTokenTypes {
    val WHITESPACE = GraphqlTokenType("GRAPHQL_WHITESPACE")
    val COMMENT = GraphqlTokenType("GRAPHQL_COMMENT")
    val STRING = GraphqlTokenType("GRAPHQL_STRING")
    val BLOCK_STRING = GraphqlTokenType("GRAPHQL_BLOCK_STRING")
    val NUMBER = GraphqlTokenType("GRAPHQL_NUMBER")
    val KEYWORD = GraphqlTokenType("GRAPHQL_KEYWORD")
    val NAME = GraphqlTokenType("GRAPHQL_NAME")
    val VARIABLE = GraphqlTokenType("GRAPHQL_VARIABLE")
    val DIRECTIVE = GraphqlTokenType("GRAPHQL_DIRECTIVE")
    val LBRACE = GraphqlTokenType("GRAPHQL_LBRACE")
    val RBRACE = GraphqlTokenType("GRAPHQL_RBRACE")
    val LPAREN = GraphqlTokenType("GRAPHQL_LPAREN")
    val RPAREN = GraphqlTokenType("GRAPHQL_RPAREN")
    val LBRACKET = GraphqlTokenType("GRAPHQL_LBRACKET")
    val RBRACKET = GraphqlTokenType("GRAPHQL_RBRACKET")
    val COLON = GraphqlTokenType("GRAPHQL_COLON")
    val EQUALS = GraphqlTokenType("GRAPHQL_EQUALS")
    val BANG = GraphqlTokenType("GRAPHQL_BANG")
    val PIPE = GraphqlTokenType("GRAPHQL_PIPE")
    val AMP = GraphqlTokenType("GRAPHQL_AMP")
    val AT = GraphqlTokenType("GRAPHQL_AT")
    val DOLLAR = GraphqlTokenType("GRAPHQL_DOLLAR")
    val SPREAD = GraphqlTokenType("GRAPHQL_SPREAD")
    val BAD_CHARACTER = GraphqlTokenType("GRAPHQL_BAD_CHARACTER")
}

/**
 * GraphQL SDL/query keywords, per the spec (`type`/`interface`/`enum`/
 * `input`/`scalar`/`union`/`schema`/`extend`/`directive` for SDL,
 * `query`/`mutation`/`subscription`/`fragment`/`on`/`true`/`false`/`null`
 * for the query language) -- a NAME token is reclassified as KEYWORD by
 * the lexer only when its text matches one of these, same "context-free
 * lexer, let the highlighter/consumer decide meaning" philosophy as
 * nginx-companion's WORD token.
 */
val GRAPHQL_KEYWORDS = setOf(
    "type", "interface", "enum", "input", "scalar", "union", "schema", "extend", "directive", "implements", "repeatable",
    "query", "mutation", "subscription", "fragment", "on", "true", "false", "null",
)
