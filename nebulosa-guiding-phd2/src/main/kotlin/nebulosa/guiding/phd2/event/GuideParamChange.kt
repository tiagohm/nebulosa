package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

data class GuideParamChange(
    @field:JsonProperty("Name") var name: String = "",
    @field:JsonProperty("Value") var value: Any? = null,
) : PHD2Event
