package nebulosa.api.atlas

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.beans.converters.angle.DegreesDeserializer
import nebulosa.api.beans.converters.angle.DegreesSerializer
import nebulosa.api.beans.converters.distance.MetersDeserializer
import nebulosa.api.beans.converters.distance.MetersSerializer
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.nova.position.GeographicCoordinate
import nebulosa.time.TimeZonedInSeconds

data class Location(
    @field:JsonSerialize(using = DegreesSerializer::class) @field:JsonDeserialize(using = DegreesDeserializer::class) override val latitude: Angle = 0.0,
    @field:JsonSerialize(using = DegreesSerializer::class) @field:JsonDeserialize(using = DegreesDeserializer::class) override val longitude: Angle = 0.0,
    @field:JsonSerialize(using = MetersSerializer::class) @field:JsonDeserialize(using = MetersDeserializer::class) override val elevation: Distance = 0.0,
    @JvmField val offsetInMinutes: Int = 0,
) : GeographicCoordinate, TimeZonedInSeconds {

    override val offsetInSeconds
        @JsonIgnore get() = offsetInMinutes * 60
}
