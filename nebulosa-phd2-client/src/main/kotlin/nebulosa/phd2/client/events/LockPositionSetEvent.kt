package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class LockPositionSetEvent(
    @field:JsonAlias("X") val x: Int = 0,
    @field:JsonAlias("Y") val y: Int = 0,
) : PHD2Event
