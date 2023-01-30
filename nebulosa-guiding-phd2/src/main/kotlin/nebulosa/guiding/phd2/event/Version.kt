package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

data class Version(
    @field:JsonProperty("PHDVersion") var version: String = "",
    @field:JsonProperty("PHDSubver") var subVersion: String = "",
) : PHD2Event
