package dev.gaphunter.graphqlcompanion.schema

/**
 * Best-effort .graphqlconfig reader -- not a full JSON-Schema-validated
 * parser, just enough structure to extract
 * `{"projects": {"<name>": {"schema": "<glob>" | ["<glob>", ...]}}}`.
 * Same "hand-rolled minimal parser instead of a new dependency" call
 * already made elsewhere in this workspace (ansible-companion's bundled
 * module index, jwt-companion's JWT payload parser).
 */
object GraphqlConfigParser {

    /** Returns null if the content doesn't look like a projects-shaped .graphqlconfig at all (e.g. the older single-project shape with a top-level "schema" key, which this parser doesn't attempt -- SchemaDiscovery's directory-grouping fallback handles that case fine). */
    fun parseProjects(content: String): Map<String, List<String>>? {
        val projectsBlock = extractObjectValue(content, "projects") ?: return null
        val entries = mutableMapOf<String, List<String>>()
        var i = skipWhitespace(projectsBlock, 1)
        if (i >= projectsBlock.length || projectsBlock[i] == '}') return entries

        while (i < projectsBlock.length) {
            i = skipWhitespace(projectsBlock, i)
            if (i >= projectsBlock.length || projectsBlock[i] != '"') break
            val (name, afterName) = readString(projectsBlock, i)
            i = skipWhitespace(projectsBlock, afterName)
            if (i >= projectsBlock.length || projectsBlock[i] != ':') break
            i = skipWhitespace(projectsBlock, i + 1)
            val (projectObj, afterObj) = readBalanced(projectsBlock, i, '{', '}') ?: break
            entries[name] = extractSchemaGlobs(projectObj)
            i = skipWhitespace(projectsBlock, afterObj)
            if (i < projectsBlock.length && projectsBlock[i] == ',') {
                i++
                continue
            }
            break
        }
        return entries
    }

    private fun extractSchemaGlobs(projectObjectText: String): List<String> {
        val schemaValue = extractRawValue(projectObjectText, "schema") ?: return emptyList()
        val trimmed = schemaValue.trim()
        return if (trimmed.startsWith("[")) {
            // Array of strings.
            Regex("\"((?:[^\"\\\\]|\\\\.)*)\"").findAll(trimmed).map { unescape(it.groupValues[1]) }.toList()
        } else if (trimmed.startsWith("\"")) {
            listOf(unescape(trimmed.trim('"')))
        } else {
            emptyList()
        }
    }

    /** Finds `"key": <value>` at any depth in the text and returns the raw (still-encoded) value text -- object/array values returned as their balanced-bracket span, strings with quotes included. */
    private fun extractRawValue(text: String, key: String): String? {
        val keyPattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*")
        val match = keyPattern.find(text) ?: return null
        val valueStart = match.range.last + 1
        if (valueStart >= text.length) return null
        return when (text[valueStart]) {
            '[' -> readBalanced(text, valueStart, '[', ']')?.first
            '{' -> readBalanced(text, valueStart, '{', '}')?.first
            '"' -> readString(text, valueStart).first.let { "\"$it\"" }
            else -> null
        }
    }

    /** Same as extractRawValue, but specifically for an object value (used for the top-level "projects" key), returning the balanced {...} span. */
    private fun extractObjectValue(text: String, key: String): String? {
        val keyPattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\\{")
        val match = keyPattern.find(text) ?: return null
        val braceStart = match.range.last
        return readBalanced(text, braceStart, '{', '}')?.first
    }

    private fun readBalanced(s: String, start: Int, open: Char, close: Char): Pair<String, Int>? {
        if (start >= s.length || s[start] != open) return null
        var depth = 0
        var i = start
        while (i < s.length) {
            when (s[i]) {
                '"' -> i = readString(s, i).second - 1
                open -> depth++
                close -> {
                    depth--
                    if (depth == 0) return s.substring(start, i + 1) to (i + 1)
                }
            }
            i++
        }
        return null
    }

    private fun readString(s: String, start: Int): Pair<String, Int> {
        var i = start + 1
        val sb = StringBuilder()
        while (i < s.length) {
            when (s[i]) {
                '"' -> return sb.toString() to (i + 1)
                '\\' -> {
                    if (i + 1 < s.length) {
                        sb.append(s[i]).append(s[i + 1])
                        i += 2
                        continue
                    }
                }
                else -> sb.append(s[i])
            }
            i++
        }
        return sb.toString() to i
    }

    private fun unescape(s: String): String = s.replace("\\\"", "\"").replace("\\\\", "\\")

    private fun skipWhitespace(s: String, from: Int): Int {
        var i = from
        while (i < s.length && s[i].isWhitespace()) i++
        return i
    }
}
