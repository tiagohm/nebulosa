package nebulosa.guiding.phd2.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class Calibrating(
    @field:JsonProperty("Mount") var mount: String = "",
    @field:JsonProperty("dir") var direction: String = "",
    @field:JsonProperty("dist") var distance: Double = 0.0,
    @field:JsonProperty("dx") var dx: Double = 0.0,
    @field:JsonProperty("dy") var dy: Double = 0.0,
    @field:JsonDeserialize(using = StarCoordinate.Deserializer::class)
    @field:JsonProperty("pos") var position: StarCoordinate = StarCoordinate.EMPTY,
    @field:JsonProperty("step") var step: Int = 0,
    @field:JsonProperty("State") var state: String = "",
) : PHD2Event
