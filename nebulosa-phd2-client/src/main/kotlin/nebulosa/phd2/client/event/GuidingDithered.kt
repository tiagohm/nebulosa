package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class GuidingDithered(
    @field:JsonProperty("dx") var dx: Double = 0.0,
    @field:JsonProperty("dy") var dy: Double = 0.0,
) : PHD2Event
