package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class CalibratingEvent(
    @field:JsonAlias("Mount") val mount: String = "",
    @field:JsonAlias("dir") val direction: String = "",
    @field:JsonAlias("dist") val distance: Double = 0.0,
    @field:JsonAlias("dx") val dx: Double = 0.0,
    @field:JsonAlias("dy") val dy: Double = 0.0,
    @field:JsonDeserialize(using = StarPosition.Deserializer::class)
    @field:JsonAlias("pos") val position: StarPosition = StarPosition.ZERO,
    @field:JsonProperty("step") val step: Int = 0,
    @field:JsonAlias("State") val state: String = "",
) : PHD2Event
