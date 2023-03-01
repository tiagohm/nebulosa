package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class StarSelected(
    @field:JsonProperty("X") var x: Int = 0,
    @field:JsonProperty("Y") var y: Int = 0,
) : PHD2Event
