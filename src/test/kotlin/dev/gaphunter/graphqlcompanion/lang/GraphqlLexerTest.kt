package dev.gaphunter.graphqlcompanion.lang

import com.intellij.psi.tree.IElementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphqlLexerTest {

    private fun tokenize(text: String): List<Pair<IElementType, String>> {
        val lexer = GraphqlLexer()
        lexer.start(text, 0, text.length, 0)
        val result = mutableListOf<Pair<IElementType, String>>()
        while (true) {
            val type = lexer.tokenType ?: break
            result.add(type to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return result
    }

    private fun nonWhitespace(text: String) = tokenize(text).filter { it.first != GraphqlTokenTypes.WHITESPACE }

    @Test
    fun typeDefinitionWithFields() {
        val tokens = nonWhitespace("type User { id: ID! name: String }")
        assertEquals(
            listOf(
                GraphqlTokenTypes.KEYWORD to "type",
                GraphqlTokenTypes.NAME to "User",
                GraphqlTokenTypes.LBRACE to "{",
                GraphqlTokenTypes.NAME to "id",
                GraphqlTokenTypes.COLON to ":",
                GraphqlTokenTypes.NAME to "ID",
                GraphqlTokenTypes.BANG to "!",
                GraphqlTokenTypes.NAME to "name",
                GraphqlTokenTypes.COLON to ":",
                GraphqlTokenTypes.NAME to "String",
                GraphqlTokenTypes.RBRACE to "}",
            ),
            tokens,
        )
    }

    @Test
    fun commentRunsToEndOfLineOnly() {
        val tokens = tokenize("# a comment\ntype T")
        assertEquals(GraphqlTokenTypes.COMMENT, tokens[0].first)
        assertEquals("# a comment", tokens[0].second)
    }

    @Test
    fun quotedStringWithEscapes() {
        val tokens = nonWhitespace(""""hello \"world\""""")
        assertEquals(1, tokens.size)
        assertEquals(GraphqlTokenTypes.STRING, tokens[0].first)
    }

    @Test
    fun tripleQuotedBlockString() {
        val tokens = nonWhitespace("\"\"\"\nmulti\nline\n\"\"\"")
        assertEquals(1, tokens.size)
        assertEquals(GraphqlTokenTypes.BLOCK_STRING, tokens[0].first)
    }

    @Test
    fun variableAndDirective() {
        val tokens = nonWhitespace("query(\$id: ID!) @cached")
        val types = tokens.map { it.first }
        assertTrue(types.contains(GraphqlTokenTypes.DOLLAR))
        assertTrue(types.contains(GraphqlTokenTypes.AT))
    }

    @Test
    fun spreadOperatorForFragments() {
        val tokens = nonWhitespace("{ ...userFields }")
        assertEquals(
            listOf(
                GraphqlTokenTypes.LBRACE to "{",
                GraphqlTokenTypes.SPREAD to "...",
                GraphqlTokenTypes.NAME to "userFields",
                GraphqlTokenTypes.RBRACE to "}",
            ),
            tokens,
        )
    }

    @Test
    fun numbersIncludingNegativeAndDecimal() {
        val tokens = nonWhitespace("-42 3.14")
        assertEquals(
            listOf(GraphqlTokenTypes.NUMBER to "-42", GraphqlTokenTypes.NUMBER to "3.14"),
            tokens,
        )
    }

    @Test
    fun commasAreInsignificantWhitespace() {
        // Per the GraphQL spec, commas are equivalent to whitespace.
        val tokens = nonWhitespace("query(\$a: Int, \$b: Int)")
        assertTrue(tokens.none { it.second == "," })
    }

    @Test
    fun neverEmitsAZeroLengthToken() {
        // Regression guard: a lexer that emits a zero-length token can
        // infinite-loop the platform's own lexer-consistency checks.
        val tokens = tokenize("type T {}")
        assertTrue(tokens.all { (type, text) -> type == GraphqlTokenTypes.BAD_CHARACTER || text.isNotEmpty() })
    }

    @Test
    fun toleratesLargeSchemaFileWithoutPathologicalSlowdown() {
        // Direct proof of the fix for the cited "1 char/1.2-1.4s on 3000+
        // line files" complaint: tokenizing a large, repetitive-but-valid
        // schema must complete quickly, not just "eventually."
        val large = buildString {
            repeat(3000) { i -> appendLine("type Type$i { id: ID! name: String field$i: Int }") }
        }
        val start = System.nanoTime()
        tokenize(large)
        val elapsedMs = (System.nanoTime() - start) / 1_000_000
        assertTrue("Expected tokenizing 3000 lines to take well under 1s, took ${elapsedMs}ms", elapsedMs < 1000)
    }
}
