package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty

data class SettleDone(
    @field:JsonProperty("Status") var status: Int = 0,
    @field:JsonProperty("TotalFrames") var totalFrames: Int = 0,
    @field:JsonProperty("DroppedFrames") var droppedFrames: Int = 0,
    @field:JsonProperty("Error") var error: String = "",
) : PHD2Event
