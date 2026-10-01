package dev.gaphunter.graphqlcompanion.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

/**
 * v0.1 has no user-configurable rules. `lastDetectedGroupSummary` is kept only so existing settings files still load:
 * nothing ever wrote it, and since 0.1.2 the Settings page computes the groups itself (see ProjectSchemaGroups).
 */
@State(name = "GraphqlCompanionSettings", storages = [Storage("graphqlCompanion.xml")])
class GraphqlCompanionSettings : PersistentStateComponent<GraphqlCompanionSettings.State> {

    class State {
        var lastDetectedGroupSummary: String = ""
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): GraphqlCompanionSettings = service()
    }
}
