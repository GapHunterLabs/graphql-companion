package dev.gaphunter.graphqlcompanion.lang

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * Deliberately flat, same reasoning as nginx-companion's
 * NginxParserDefinition: v0.1's feature set (fast lexer-based
 * highlighting + schema discovery) doesn't need a real grammar tree
 * (matching braces, distinguishing type defs from field defs, etc.).
 * `createElement` returns a real ASTWrapperPsiElement, not a thrown
 * exception -- nginx-companion's own KNOWN_ISSUES.md documents that a
 * throwing implementation silently aborts PSI construction for the whole
 * file type, with no highlighting at all and no obvious error.
 */
class GraphqlParserDefinition : ParserDefinition {

    companion object {
        val FILE = IFileElementType(GraphqlLanguage)
    }

    override fun createLexer(project: Project): Lexer = GraphqlLexer()

    override fun createParser(project: Project): PsiParser = PsiParser { root, builder ->
        val marker = builder.mark()
        while (!builder.eof()) {
            builder.advanceLexer()
        }
        marker.done(root)
        builder.treeBuilt
    }

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = TokenSet.create(GraphqlTokenTypes.COMMENT)

    override fun getStringLiteralElements(): TokenSet =
        TokenSet.create(GraphqlTokenTypes.STRING, GraphqlTokenTypes.BLOCK_STRING)

    override fun getWhitespaceTokens(): TokenSet =
        TokenSet.create(GraphqlTokenTypes.WHITESPACE, TokenType.WHITE_SPACE)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = GraphqlFile(viewProvider)

    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)
}
