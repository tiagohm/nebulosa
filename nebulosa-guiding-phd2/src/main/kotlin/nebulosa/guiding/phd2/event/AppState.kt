package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

data class AppState(
    @field:JsonProperty("State") var state: GuideState = GuideState.STOPPED,
) : PHD2Event
