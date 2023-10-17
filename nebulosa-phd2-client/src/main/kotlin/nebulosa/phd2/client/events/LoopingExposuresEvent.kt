package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class LoopingExposuresEvent(
    @field:JsonAlias("Frame") val frame: Int = 0,
) : PHD2Event
