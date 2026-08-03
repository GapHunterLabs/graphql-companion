package dev.gaphunter.graphqlcompanion.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

/** Read-only in v0.1: shows the last-detected schema groups so multi-schema scoping is visible/discoverable, not a black box -- the direct fix for "the documentation for setting up scopes is terrible" (users can SEE what was detected without reading docs). No settings to actually change yet. */
class GraphqlCompanionConfigurable : Configurable {

    override fun getDisplayName(): String = "GraphQL Companion"

    override fun createComponent(): JComponent {
        val panel = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }
        val summary = GraphqlCompanionSettings.getInstance().state.lastDetectedGroupSummary
        val text = summary.ifEmpty { "No schema files detected yet in this project." }
        panel.add(JBLabel("Detected schema groups:"))
        panel.add(JBLabel(text))
        return panel
    }

    override fun isModified(): Boolean = false
    override fun apply() {}
    override fun reset() {}
}
