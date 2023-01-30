package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

data class CalibrationComplete(
    @field:JsonProperty("Mount") var mount: String = "",
) : PHD2Event
