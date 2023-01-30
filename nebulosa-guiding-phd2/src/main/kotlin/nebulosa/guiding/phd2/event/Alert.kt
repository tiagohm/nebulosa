package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

data class Alert(
    @field:JsonProperty("Msg") var message: String = "",
    @field:JsonProperty("Type") var type: String = "",
) : PHD2Event
