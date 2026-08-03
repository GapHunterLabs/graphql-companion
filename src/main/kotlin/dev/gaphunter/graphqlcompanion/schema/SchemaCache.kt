package dev.gaphunter.graphqlcompanion.schema

import java.util.concurrent.ConcurrentHashMap

/**
 * Same modification-stamp + fingerprint invalidation shape as
 * highlight-companion's ComplexityCache, adapted for a computation that
 * spans a whole SET of files (schema discovery) rather than one PSI
 * element. Keyed by project base path so multiple open projects never
 * collide; invalidated whenever the set of known schema file paths (or
 * their timestamps) actually changes, not on every keystroke inside one
 * schema file -- discovery is a directory-shape computation, editing the
 * body of one file never changes it.
 */
object SchemaCache {
    private data class Entry(val fingerprint: Int, val groups: List<SchemaGroup>)

    private val cache = ConcurrentHashMap<String, Entry>()

    fun getOrCompute(projectKey: String, filePathsWithTimestamps: List<Pair<String, Long>>, compute: () -> List<SchemaGroup>): List<SchemaGroup> {
        val fingerprint = filePathsWithTimestamps.sortedBy { it.first }.hashCode()
        val cached = cache[projectKey]
        if (cached != null && cached.fingerprint == fingerprint) {
            return cached.groups
        }
        val groups = compute()
        cache[projectKey] = Entry(fingerprint, groups)
        return groups
    }

    fun invalidate(projectKey: String) {
        cache.remove(projectKey)
    }
}
