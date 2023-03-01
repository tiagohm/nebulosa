package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class Version(
    @field:JsonProperty("PHDVersion") var version: String = "",
    @field:JsonProperty("PHDSubver") var subVersion: String = "",
) : PHD2Event
