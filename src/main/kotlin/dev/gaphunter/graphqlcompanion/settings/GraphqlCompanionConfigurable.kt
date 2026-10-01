package dev.gaphunter.graphqlcompanion.settings

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.openapi.util.text.StringUtil
import dev.gaphunter.graphqlcompanion.schema.ProjectSchemaGroups
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Settings > Tools > GraphQL Companion: the schema groups detected in this
 * project. A project-level page (it shows one project's files); the
 * detection runs in a background read action, never on the EDT, and the
 * label is filled in when it finishes.
 */
class GraphqlCompanionConfigurable(private val project: Project) : Configurable {

    override fun getDisplayName(): String = "GraphQL Companion"

    override fun createComponent(): JComponent {
        val panel = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }
        panel.add(JBLabel("Detected schema groups:"))
        val groupsLabel = JBLabel("Detecting…")
        panel.add(groupsLabel)
        ReadAction.nonBlocking<List<String>> { ProjectSchemaGroups.summary(ProjectSchemaGroups.detect(project)) }
            .inSmartMode(project)
            .expireWith(project)
            .finishOnUiThread(ModalityState.any()) { lines ->
                groupsLabel.text = if (lines.isEmpty()) {
                    "No .graphql/.graphqls files in this project."
                } else {
                    "<html>" + lines.joinToString("<br>") { StringUtil.escapeXmlEntities(it) } + "</html>"
                }
            }
            .submit(AppExecutorUtil.getAppExecutorService())
        return panel
    }

    override fun isModified(): Boolean = false

    override fun apply() {}

    override fun reset() {}
}
