package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class LoopingExposures(
    @field:JsonProperty("Frame") val frame: Int = 0,
) : PHD2Event
