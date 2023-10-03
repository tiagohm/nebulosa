package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class CalibrationFailed(
    @field:JsonProperty("Reason") val reason: String = "",
) : PHD2Event
