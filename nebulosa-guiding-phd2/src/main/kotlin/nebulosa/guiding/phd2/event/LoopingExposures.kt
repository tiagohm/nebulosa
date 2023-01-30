package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

data class LoopingExposures(
    @field:JsonProperty("Frame") var frame: Int = 0,
) : PHD2Event
