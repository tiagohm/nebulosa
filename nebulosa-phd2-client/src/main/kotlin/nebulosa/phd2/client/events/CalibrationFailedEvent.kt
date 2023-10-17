package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class CalibrationFailedEvent(
    @field:JsonAlias("Reason") val reason: String = "",
) : PHD2Event
