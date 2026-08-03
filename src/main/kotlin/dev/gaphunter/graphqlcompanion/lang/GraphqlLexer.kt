package dev.gaphunter.graphqlcompanion.lang

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

/**
 * Hand-rolled, single-pass lexer for GraphQL SDL/query syntax -- same
 * shape as nginx-companion's NginxLexer, and the direct, structural fix
 * for the competitor's cited "1 char per 1.2-1.4s, no syntax highlighting
 * on 3000+ line files" complaint: a purpose-built scanner for GraphQL's
 * SDL grammar (comments, strings, block strings, numbers, punctuation,
 * names/keywords) is orders of magnitude cheaper per keystroke than a
 * general-purpose incremental parser struggling on scope resolution. No
 * backtracking, no lookahead beyond a handful of characters.
 */
class GraphqlLexer : LexerBase() {

    private lateinit var buffer: CharSequence
    private var startOffset = 0
    private var endOffset = 0

    private var tokenStart = 0
    private var tokenEnd = 0
    private var tokenType: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        tokenStart = startOffset
        tokenEnd = startOffset
        locateToken()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = tokenType

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun advance() {
        tokenStart = tokenEnd
        locateToken()
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = endOffset

    private fun locateToken() {
        if (tokenStart >= endOffset) {
            tokenType = null
            tokenEnd = tokenStart
            return
        }
        val c = buffer[tokenStart]
        when {
            c.isWhitespace() || c == ',' -> {
                // GraphQL treats commas as insignificant whitespace, per spec.
                tokenType = GraphqlTokenTypes.WHITESPACE
                tokenEnd = scanWhile(tokenStart) { it.isWhitespace() || it == ',' }
            }
            c == '#' -> {
                tokenType = GraphqlTokenTypes.COMMENT
                tokenEnd = scanWhile(tokenStart + 1) { it != '\n' }
            }
            c == '"' && startsWithTripleQuote(tokenStart) -> {
                tokenType = GraphqlTokenTypes.BLOCK_STRING
                tokenEnd = scanBlockString(tokenStart)
            }
            c == '"' -> {
                tokenType = GraphqlTokenTypes.STRING
                tokenEnd = scanQuotedString(tokenStart)
            }
            c == '.' && startsWithSpread(tokenStart) -> {
                tokenType = GraphqlTokenTypes.SPREAD
                tokenEnd = tokenStart + 3
            }
            c.isDigit() || (c == '-' && tokenStart + 1 < endOffset && buffer[tokenStart + 1].isDigit()) -> {
                tokenType = GraphqlTokenTypes.NUMBER
                tokenEnd = scanNumber(tokenStart)
            }
            c == '$' -> {
                tokenType = GraphqlTokenTypes.DOLLAR
                tokenEnd = tokenStart + 1
            }
            c == '@' -> {
                tokenType = GraphqlTokenTypes.AT
                tokenEnd = tokenStart + 1
            }
            c == '{' -> singleChar(GraphqlTokenTypes.LBRACE)
            c == '}' -> singleChar(GraphqlTokenTypes.RBRACE)
            c == '(' -> singleChar(GraphqlTokenTypes.LPAREN)
            c == ')' -> singleChar(GraphqlTokenTypes.RPAREN)
            c == '[' -> singleChar(GraphqlTokenTypes.LBRACKET)
            c == ']' -> singleChar(GraphqlTokenTypes.RBRACKET)
            c == ':' -> singleChar(GraphqlTokenTypes.COLON)
            c == '=' -> singleChar(GraphqlTokenTypes.EQUALS)
            c == '!' -> singleChar(GraphqlTokenTypes.BANG)
            c == '|' -> singleChar(GraphqlTokenTypes.PIPE)
            c == '&' -> singleChar(GraphqlTokenTypes.AMP)
            isNameStart(c) -> {
                tokenEnd = scanWhile(tokenStart) { isNameContinue(it) }
                val text = buffer.subSequence(tokenStart, tokenEnd).toString()
                tokenType = if (text in GRAPHQL_KEYWORDS) GraphqlTokenTypes.KEYWORD else GraphqlTokenTypes.NAME
            }
            else -> {
                tokenType = GraphqlTokenTypes.BAD_CHARACTER
                tokenEnd = tokenStart + 1
            }
        }
        if (tokenEnd <= tokenStart) {
            // Safety net: never emit a zero-length token.
            tokenType = GraphqlTokenTypes.BAD_CHARACTER
            tokenEnd = tokenStart + 1
        }
    }

    private fun singleChar(type: IElementType) {
        tokenType = type
        tokenEnd = tokenStart + 1
    }

    private fun isNameStart(c: Char): Boolean = c.isLetter() || c == '_'
    private fun isNameContinue(c: Char): Boolean = c.isLetterOrDigit() || c == '_'

    private fun scanWhile(from: Int, predicate: (Char) -> Boolean): Int {
        var i = from
        while (i < endOffset && predicate(buffer[i])) i++
        return i
    }

    private fun startsWithTripleQuote(at: Int): Boolean =
        at + 2 < endOffset && buffer[at] == '"' && buffer[at + 1] == '"' && buffer[at + 2] == '"'

    private fun startsWithSpread(at: Int): Boolean =
        at + 2 < endOffset && buffer[at] == '.' && buffer[at + 1] == '.' && buffer[at + 2] == '.'

    private fun scanQuotedString(from: Int): Int {
        var i = from + 1
        while (i < endOffset) {
            val c = buffer[i]
            if (c == '\\' && i + 1 < endOffset) {
                i += 2
                continue
            }
            if (c == '"') return i + 1
            if (c == '\n') return i // Unterminated on this line.
            i++
        }
        return i
    }

    private fun scanBlockString(from: Int): Int {
        var i = from + 3
        while (i + 2 < endOffset) {
            if (buffer[i] == '"' && buffer[i + 1] == '"' && buffer[i + 2] == '"') return i + 3
            i++
        }
        return endOffset
    }

    private fun scanNumber(from: Int): Int {
        var i = from
        if (buffer[i] == '-') i++
        i = scanWhile(i) { it.isDigit() }
        if (i < endOffset && buffer[i] == '.') {
            i++
            i = scanWhile(i) { it.isDigit() }
        }
        if (i < endOffset && (buffer[i] == 'e' || buffer[i] == 'E')) {
            i++
            if (i < endOffset && (buffer[i] == '+' || buffer[i] == '-')) i++
            i = scanWhile(i) { it.isDigit() }
        }
        return i
    }
}
