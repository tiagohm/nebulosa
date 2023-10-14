package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class SettlingEvent(
    @field:JsonAlias("Distance") val distance: Double = 0.0,
    @field:JsonAlias("Time") val time: Double = 0.0,
    @field:JsonAlias("SettleTime") val settleTime: Double = 0.0,
    @field:JsonAlias("StarLocked") val starLocked: Boolean = false,
) : PHD2Event
