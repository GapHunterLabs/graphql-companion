package dev.gaphunter.graphqlcompanion.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class GraphqlFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, GraphqlLanguage) {
    override fun getFileType() = GraphqlFileType
    override fun toString(): String = "GraphQL File"
}
