package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class Alert(
    @field:JsonProperty("Msg") var message: String = "",
    @field:JsonProperty("Type") var type: String = "",
) : PHD2Event
