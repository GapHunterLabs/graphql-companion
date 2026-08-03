package dev.gaphunter.graphqlcompanion.schema

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphqlConfigParserTest {

    @Test
    fun `parses a multi-project config with array schema globs`() {
        val content = """
            {
              "projects": {
                "users": {
                  "schema": ["services/users-service/schema/*.graphqls"]
                },
                "orders": {
                  "schema": ["services/orders-service/schema/*.graphqls"]
                }
              }
            }
        """.trimIndent()
        val result = GraphqlConfigParser.parseProjects(content)
        assertEquals(setOf("users", "orders"), result?.keys)
        assertEquals(listOf("services/users-service/schema/*.graphqls"), result?.get("users"))
    }

    @Test
    fun `parses a single string schema value, not just an array`() {
        val content = """{"projects": {"main": {"schema": "schema.graphql"}}}"""
        val result = GraphqlConfigParser.parseProjects(content)
        assertEquals(listOf("schema.graphql"), result?.get("main"))
    }

    @Test
    fun `returns null when there is no top-level projects key`() {
        val content = """{"schema": "schema.graphql"}"""
        assertNull(GraphqlConfigParser.parseProjects(content))
    }

    @Test
    fun `returns an empty map for an empty projects object`() {
        val content = """{"projects": {}}"""
        val result = GraphqlConfigParser.parseProjects(content)
        assertTrue(result != null && result.isEmpty())
    }

    @Test
    fun `handles extra whitespace and formatting variations`() {
        val content = """
            {
                "projects"   :   {
                    "a" : { "schema" : [ "x.graphql" ] }
                }
            }
        """.trimIndent()
        val result = GraphqlConfigParser.parseProjects(content)
        assertEquals(listOf("x.graphql"), result?.get("a"))
    }
}
