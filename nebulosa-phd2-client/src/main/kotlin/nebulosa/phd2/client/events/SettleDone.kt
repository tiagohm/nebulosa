package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class SettleDone(
    @field:JsonProperty("Status") val status: Int = 0,
    @field:JsonProperty("TotalFrames") val totalFrames: Int = 0,
    @field:JsonProperty("DroppedFrames") val droppedFrames: Int = 0,
    @field:JsonProperty("Error") val error: String = "",
) : PHD2Event
