package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class Alert(
    @field:JsonProperty("Msg") val message: String = "",
    @field:JsonProperty("Type") val type: AlertType = AlertType.INFO,
) : PHD2Event
