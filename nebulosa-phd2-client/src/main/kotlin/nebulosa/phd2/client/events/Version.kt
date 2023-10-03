package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class Version(
    @field:JsonProperty("PHDVersion") val version: String = "",
    @field:JsonProperty("PHDSubver") val subVersion: String = "",
    @field:JsonProperty("OverlapSupport") val overlapSupport: Boolean = false,
) : PHD2Event
