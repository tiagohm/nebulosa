package nebulosa.api.atlas

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import nebulosa.api.beans.converters.angle.DegreesDeserializer
import nebulosa.api.beans.converters.distance.MetersDeserializer
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.nova.position.GeographicCoordinate
import nebulosa.time.TimeZonedInSeconds

data class Location(
    @field:JsonDeserialize(using = DegreesDeserializer::class) override val latitude: Angle = 0.0,
    @field:JsonDeserialize(using = DegreesDeserializer::class) override val longitude: Angle = 0.0,
    @field:JsonDeserialize(using = MetersDeserializer::class) override val elevation: Distance = 0.0,
    val offsetInMinutes: Int = 0,
) : GeographicCoordinate, TimeZonedInSeconds {

    override val offsetInSeconds = offsetInMinutes * 60
}
