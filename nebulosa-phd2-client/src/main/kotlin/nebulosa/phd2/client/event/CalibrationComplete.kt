package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class CalibrationComplete(
    @field:JsonProperty("Mount") var mount: String = "",
) : PHD2Event
