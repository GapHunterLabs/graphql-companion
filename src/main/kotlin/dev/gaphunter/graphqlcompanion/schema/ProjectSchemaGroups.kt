package dev.gaphunter.graphqlcompanion.schema

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import dev.gaphunter.graphqlcompanion.lang.GraphqlFileType

/**
 * The schema groups of an open project, as shown in Settings > Tools >
 * GraphQL Companion: every `.graphql`/`.graphqls` file of the project,
 * grouped by [SchemaDiscovery] (the `projects` of a `.graphqlconfig` at
 * the project root when there is one, otherwise by directory).
 *
 * Fixes a real bug found while preparing the product demo (2026-10-01):
 * the discovery existed and was unit-tested, but nothing ever ran it --
 * the Settings page read a summary that no code wrote, so it always said
 * "No schema files detected yet in this project.".
 *
 * Uses the file-type index: needs a read action and smart mode, and must
 * not run on the EDT.
 */
object ProjectSchemaGroups {

    private val LOG = logger<ProjectSchemaGroups>()

    fun detect(project: Project): List<SchemaGroup> {
        val index = ProjectRootManager.getInstance(project).fileIndex
        val files = FileTypeIndex.getFiles(GraphqlFileType, GlobalSearchScope.projectScope(project))
        val paths = files.mapNotNull { file ->
            ProgressManager.checkCanceled()
            val root = index.getContentRootForFile(file) ?: return@mapNotNull null
            VfsUtilCore.getRelativePath(file, root)?.let { it to file.timeStamp }
        }.sortedBy { it.first }
        val configText = project.guessProjectDir()?.findChild(".graphqlconfig")?.let { config ->
            runCatching { VfsUtilCore.loadText(config) }.getOrNull()
        }
        val configProjects = configText?.let { GraphqlConfigParser.parseProjects(it) }
        LOG.info("schema files: ${files.size} found, relative paths ${paths.map { it.first }}, .graphqlconfig projects ${configProjects?.keys}")
        val fingerprintInputs = paths + listOf((".graphqlconfig" to (configText?.hashCode()?.toLong() ?: 0L)))
        return SchemaCache.getOrCompute(project.locationHash, fingerprintInputs) {
            SchemaDiscovery.discover(paths.map { it.first }, configProjects)
        }
    }

    /** One line per group: "orders-service — 2 files: services/orders-service/schema/orders.graphqls, ...". */
    fun summary(groups: List<SchemaGroup>, maxFilesShown: Int = 3): List<String> = groups.map { group ->
        val count = group.filePaths.size
        val shown = group.filePaths.take(maxFilesShown).joinToString(", ")
        val more = if (count > maxFilesShown) ", …" else ""
        "${group.name} — $count file${if (count == 1) "" else "s"}: $shown$more"
    }
}
