package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias
import nebulosa.guiding.GuideState

data class AppStateEvent(
    @field:JsonAlias("State") val state: GuideState = GuideState.STOPPED,
) : PHD2Event
