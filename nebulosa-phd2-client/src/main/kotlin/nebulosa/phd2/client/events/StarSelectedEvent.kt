package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias
import nebulosa.guiding.GuidePoint

data class StarSelectedEvent(
    @field:JsonAlias("X") override val x: Int = 0,
    @field:JsonAlias("Y") override val y: Int = 0,
) : PHD2Event, GuidePoint
