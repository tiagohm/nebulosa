package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

data class CalibrationFailed(
    @field:JsonProperty("Reason") var reason: String = "",
) : PHD2Event
