package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias

data class GuideParamChangeEvent(
    @field:JsonAlias("Name") val name: String = "",
    @field:JsonAlias("Value") val value: Any? = null,
) : PHD2Event
