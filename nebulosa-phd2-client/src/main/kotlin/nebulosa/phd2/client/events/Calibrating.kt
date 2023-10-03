package nebulosa.phd2.client.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class Calibrating(
    @field:JsonProperty("Mount") val mount: String = "",
    @field:JsonProperty("dir") val direction: String = "",
    @field:JsonProperty("dist") val distance: Double = 0.0,
    @field:JsonProperty("dx") val dx: Double = 0.0,
    @field:JsonProperty("dy") val dy: Double = 0.0,
    @field:JsonDeserialize(using = StarCoordinate.Deserializer::class)
    @field:JsonProperty("pos") val position: StarCoordinate = StarCoordinate.EMPTY,
    @field:JsonProperty("step") val step: Int = 0,
    @field:JsonProperty("State") val state: String = "",
) : PHD2Event
