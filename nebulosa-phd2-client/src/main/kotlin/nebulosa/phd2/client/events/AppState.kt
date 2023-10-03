package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class AppState(
    @field:JsonProperty("State") val state: State = State.STOPPED,
) : PHD2Event
