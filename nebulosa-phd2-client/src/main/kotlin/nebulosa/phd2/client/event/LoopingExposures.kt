package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class LoopingExposures(
    @field:JsonProperty("Frame") var frame: Int = 0,
) : PHD2Event
