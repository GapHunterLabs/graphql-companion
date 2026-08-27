package dev.gaphunter.graphqlcompanion.schema

/**
 * Pure Kotlin, no VirtualFile/PSI dependency -- the direct fix for the
 * competitor's cited "does not work in projects with multiple schemas"
 * complaint. Groups .graphql/.graphqls file paths into named scopes,
 * either from an explicit .graphqlconfig ("projects": {name: {schema: [...]}})
 * or, when no config exists, by grouping every schema file under the same
 * top-level directory -- so multi-schema projects work out of the box
 * with zero configuration, not just when a .graphqlconfig happens to
 * exist.
 */

data class SchemaGroup(val name: String, val filePaths: List<String>)

object SchemaDiscovery {

    /**
     * @param configProjects result of GraphqlConfigParser.parse() on a
     * .graphqlconfig file's content, if one was found; null if none exists
     * or it couldn't be parsed.
     */
    fun discover(allSchemaFilePaths: List<String>, configProjects: Map<String, List<String>>?): List<SchemaGroup> {
        if (!configProjects.isNullOrEmpty()) {
            return configProjects.map { (name, globs) ->
                SchemaGroup(name, allSchemaFilePaths.filter { path -> globs.any { glob -> matchesGlob(path, glob) } })
            }
        }
        return groupByTopLevelDirectory(allSchemaFilePaths)
    }

    /** Fallback with no config: group by the first path segment after any common root -- e.g. "services/users-service/schema/users.graphqls" and "services/orders-service/schema/orders.graphqls" become two separate groups (users-service, orders-service), not one flat pile. */
    private fun groupByTopLevelDirectory(paths: List<String>): List<SchemaGroup> {
        val normalized = paths.map { it.replace('\\', '/') }
        val grouped = normalized.groupBy { path ->
            val segments = path.split('/')
            // Prefer the segment before "schema"/"graphql" if present (the
            // real-world convention this workspace's own demo layout
            // follows), otherwise the first directory segment.
            val schemaIdx = segments.indexOfFirst { it == "schema" || it == "graphql" }
            when {
                schemaIdx > 0 -> segments[schemaIdx - 1]
                segments.size > 1 -> segments[0]
                else -> "default"
            }
        }
        return grouped.map { (name, groupPaths) -> SchemaGroup(name, groupPaths) }.sortedBy { it.name }
    }

    // Minimal glob support: a single star matches any run of characters
    // within one path segment, a double star matches across segments --
    // enough for typical .graphqlconfig schema glob patterns, without a
    // full glob library dependency. (Deliberately a line comment, not a
    // KDoc block: a literal star-slash inside an example glob pattern
    // would close a /** block comment early -- confirmed the hard way
    // while writing this exact function.)
    private fun matchesGlob(path: String, glob: String): Boolean {
        val normalizedPath = path.replace('\\', '/')
        val normalizedGlob = glob.replace('\\', '/')
        val regex = Regex(
            normalizedGlob.split("**").joinToString(".*") { segment ->
                segment.split("*").joinToString(".*") { literal -> Regex.escape(literal) }
            },
        )
        return regex.matches(normalizedPath)
    }
}
