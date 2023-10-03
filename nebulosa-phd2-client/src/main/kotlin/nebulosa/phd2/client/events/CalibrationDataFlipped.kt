package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class CalibrationDataFlipped(
    @field:JsonProperty("Mount") val mount: String = "",
) : PHD2Event
