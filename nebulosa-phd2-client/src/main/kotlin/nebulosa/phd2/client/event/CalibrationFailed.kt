package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class CalibrationFailed(
    @field:JsonProperty("Reason") var reason: String = "",
) : PHD2Event
