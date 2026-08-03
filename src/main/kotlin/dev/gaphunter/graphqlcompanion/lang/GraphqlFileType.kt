package dev.gaphunter.graphqlcompanion.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * Deliberately plain extension-based registration (plugin.xml
 * `<fileType>` with `extensions="graphql;graphqls"`), NOT a
 * FileTypeOverrider/FileTypeIdentifiableByVirtualFile -- unlike
 * nginx-companion's `.conf` (contested by bundled TextMate), no bundled
 * plugin claims `.graphql`/`.graphqls`, so there's no priority race to
 * win and no reason to take on that extra complexity/gotcha surface.
 */
object GraphqlFileType : LanguageFileType(GraphqlLanguage) {
    override fun getName(): String = "GraphQL"
    override fun getDescription(): String = "GraphQL schema or query document"
    override fun getDefaultExtension(): String = "graphql"
    override fun getIcon(): Icon? = null
}
