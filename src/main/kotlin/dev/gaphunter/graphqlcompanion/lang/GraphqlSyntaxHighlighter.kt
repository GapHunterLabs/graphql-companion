package dev.gaphunter.graphqlcompanion.lang

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType

object GraphqlHighlighterColors {
    val COMMENT: TextAttributesKey = createTextAttributesKey("GRAPHQL_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val STRING: TextAttributesKey = createTextAttributesKey("GRAPHQL_STRING", DefaultLanguageHighlighterColors.STRING)
    val NUMBER: TextAttributesKey = createTextAttributesKey("GRAPHQL_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val KEYWORD: TextAttributesKey = createTextAttributesKey("GRAPHQL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val NAME: TextAttributesKey = createTextAttributesKey("GRAPHQL_NAME", DefaultLanguageHighlighterColors.IDENTIFIER)
    val VARIABLE: TextAttributesKey = createTextAttributesKey("GRAPHQL_VARIABLE", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    val DIRECTIVE: TextAttributesKey = createTextAttributesKey("GRAPHQL_DIRECTIVE", DefaultLanguageHighlighterColors.METADATA)
    val BRACES: TextAttributesKey = createTextAttributesKey("GRAPHQL_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val PARENTHESES: TextAttributesKey = createTextAttributesKey("GRAPHQL_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    val BRACKETS: TextAttributesKey = createTextAttributesKey("GRAPHQL_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val OPERATOR: TextAttributesKey = createTextAttributesKey("GRAPHQL_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val BAD_CHARACTER: TextAttributesKey = createTextAttributesKey("GRAPHQL_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)
}

class GraphqlSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = GraphqlLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        val key = when (tokenType) {
            GraphqlTokenTypes.COMMENT -> GraphqlHighlighterColors.COMMENT
            GraphqlTokenTypes.STRING, GraphqlTokenTypes.BLOCK_STRING -> GraphqlHighlighterColors.STRING
            GraphqlTokenTypes.NUMBER -> GraphqlHighlighterColors.NUMBER
            GraphqlTokenTypes.KEYWORD -> GraphqlHighlighterColors.KEYWORD
            GraphqlTokenTypes.NAME -> GraphqlHighlighterColors.NAME
            GraphqlTokenTypes.DOLLAR -> GraphqlHighlighterColors.VARIABLE
            GraphqlTokenTypes.AT -> GraphqlHighlighterColors.DIRECTIVE
            GraphqlTokenTypes.LBRACE, GraphqlTokenTypes.RBRACE -> GraphqlHighlighterColors.BRACES
            GraphqlTokenTypes.LPAREN, GraphqlTokenTypes.RPAREN -> GraphqlHighlighterColors.PARENTHESES
            GraphqlTokenTypes.LBRACKET, GraphqlTokenTypes.RBRACKET -> GraphqlHighlighterColors.BRACKETS
            GraphqlTokenTypes.COLON, GraphqlTokenTypes.EQUALS, GraphqlTokenTypes.BANG,
            GraphqlTokenTypes.PIPE, GraphqlTokenTypes.AMP, GraphqlTokenTypes.SPREAD,
            -> GraphqlHighlighterColors.OPERATOR
            GraphqlTokenTypes.BAD_CHARACTER -> GraphqlHighlighterColors.BAD_CHARACTER
            else -> return emptyArray()
        }
        return arrayOf(key)
    }
}

class GraphqlSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) = GraphqlSyntaxHighlighter()
}
