package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class AlertEvent(
    @field:JsonAlias("Msg") val message: String = "",
    @field:JsonAlias("Type") val type: AlertType = AlertType.INFO,
) : PHD2Event
