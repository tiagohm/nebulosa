package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class SettleDoneEvent(
    @field:JsonAlias("Status") val status: Int = 0,
    @field:JsonAlias("TotalFrames") val totalFrames: Int = 0,
    @field:JsonAlias("DroppedFrames") val droppedFrames: Int = 0,
    @field:JsonAlias("Error") val error: String = "",
) : PHD2Event
