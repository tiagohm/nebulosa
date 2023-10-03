package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class GuideParamChange(
    @field:JsonProperty("Name") val name: String = "",
    @field:JsonProperty("Value") val value: Any? = null,
) : PHD2Event
