package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class StarSelected(
    @field:JsonProperty("X") val x: Int = 0,
    @field:JsonProperty("Y") val y: Int = 0,
) : PHD2Event
