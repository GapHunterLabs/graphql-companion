package dev.gaphunter.graphqlcompanion.schema

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Regression test for a real bug found while preparing the product demo
 * (2026-10-01): the schema discovery was never run on a project -- the
 * Settings page showed a summary nothing ever wrote. This runs the real
 * detection over the project's files, the same call the page makes.
 */
class ProjectSchemaGroupsTest : BasePlatformTestCase() {

    fun testGroupsTheProjectsSchemaFilesByDirectory() {
        myFixture.addFileToProject("services/orders-service/schema/orders.graphqls", "type Order { id: ID! }\n")
        myFixture.addFileToProject("services/users-service/schema/users.graphqls", "type User { id: ID! }\n")
        myFixture.addFileToProject("shared/schema/common.graphqls", "scalar DateTime\n")
        myFixture.addFileToProject("notes.txt", "not a schema\n")

        val groups = ProjectSchemaGroups.detect(project)

        assertEquals(listOf("orders-service", "shared", "users-service"), groups.map { it.name })
        assertEquals(listOf("services/orders-service/schema/orders.graphqls"), groups.first { it.name == "orders-service" }.filePaths)
    }

    fun testNoSchemaFilesMeansNoGroups() {
        myFixture.addFileToProject("notes.txt", "not a schema\n")
        assertEquals(emptyList<SchemaGroup>(), ProjectSchemaGroups.detect(project))
    }

    fun testSummaryLinesNameEachGroupWithItsFiles() {
        val lines = ProjectSchemaGroups.summary(
            listOf(
                SchemaGroup("orders-service", listOf("a.graphqls")),
                SchemaGroup("big", listOf("1.graphqls", "2.graphqls", "3.graphqls", "4.graphqls")),
            ),
        )
        assertEquals("orders-service — 1 file: a.graphqls", lines[0])
        assertEquals("big — 4 files: 1.graphqls, 2.graphqls, 3.graphqls, …", lines[1])
    }
}
