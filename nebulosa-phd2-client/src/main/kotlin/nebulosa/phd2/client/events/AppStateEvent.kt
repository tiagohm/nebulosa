package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class AppStateEvent(
    @field:JsonAlias("State") val state: State = State.STOPPED,
) : PHD2Event
