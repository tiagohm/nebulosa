package nebulosa.phd2.client.event

import com.fasterxml.jackson.annotation.JsonProperty

data class Settling(
    @field:JsonProperty("Distance") var distance: Double = 0.0,
    @field:JsonProperty("Time") var time: Double = 0.0,
    @field:JsonProperty("SettleTime") var settleTime: Double = 0.0,
    @field:JsonProperty("StarLocked") var starLocked: Boolean = false,
) : PHD2Event
