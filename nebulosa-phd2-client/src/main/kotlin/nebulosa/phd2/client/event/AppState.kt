package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class AppState(
    @field:JsonProperty("State") var state: String = "",
) : PHD2Event
