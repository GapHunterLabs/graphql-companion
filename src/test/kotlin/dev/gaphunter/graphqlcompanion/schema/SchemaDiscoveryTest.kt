package dev.gaphunter.graphqlcompanion.schema

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemaDiscoveryTest {

    @Test
    fun `no config file -- groups by directory before the schema segment`() {
        val paths = listOf(
            "services/users-service/schema/users.graphqls",
            "services/orders-service/schema/orders.graphqls",
            "shared/schema/common.graphqls",
        )
        val groups = SchemaDiscovery.discover(paths, configProjects = null)
        val names = groups.map { it.name }.toSet()
        assertEquals(setOf("users-service", "orders-service", "shared"), names)
        assertEquals(1, groups.first { it.name == "users-service" }.filePaths.size)
    }

    @Test
    fun `explicit config projects take priority over directory grouping`() {
        val paths = listOf(
            "services/users-service/schema/users.graphqls",
            "services/orders-service/schema/orders.graphqls",
        )
        // Deliberately group both into ONE project via config, proving config wins.
        val config = mapOf("everything" to listOf("services/**/*.graphqls"))
        val groups = SchemaDiscovery.discover(paths, config)
        assertEquals(1, groups.size)
        assertEquals("everything", groups.first().name)
        assertEquals(2, groups.first().filePaths.size)
    }

    @Test
    fun `a single schema file with no directory structure still gets a group`() {
        val groups = SchemaDiscovery.discover(listOf("schema.graphql"), configProjects = null)
        assertEquals(1, groups.size)
        assertEquals(1, groups.first().filePaths.size)
    }

    @Test
    fun `empty file list produces no groups`() {
        assertTrue(SchemaDiscovery.discover(emptyList(), configProjects = null).isEmpty())
    }

    @Test
    fun `windows-style backslash paths are normalized before grouping`() {
        val paths = listOf(
            """services\users-service\schema\users.graphqls""",
            """services\orders-service\schema\orders.graphqls""",
        )
        val groups = SchemaDiscovery.discover(paths, configProjects = null)
        assertEquals(setOf("users-service", "orders-service"), groups.map { it.name }.toSet())
    }
}
