package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty

data class Settling(
    @field:JsonProperty("Distance") val distance: Double = 0.0,
    @field:JsonProperty("Time") val time: Double = 0.0,
    @field:JsonProperty("SettleTime") val settleTime: Double = 0.0,
    @field:JsonProperty("StarLocked") val starLocked: Boolean = false,
) : PHD2Event
