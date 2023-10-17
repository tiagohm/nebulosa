package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class GuidingDitheredEvent(
    @field:JsonProperty("dx") val dx: Double = 0.0,
    @field:JsonProperty("dy") val dy: Double = 0.0,
) : PHD2Event
