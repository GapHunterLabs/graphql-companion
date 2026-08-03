package dev.gaphunter.graphqlcompanion.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

/** v0.1 has no user-configurable rules (unlike the other 4 plugins) -- this exists to persist the last-detected schema group summary shown in the Configurable, not behavior toggles. */
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
