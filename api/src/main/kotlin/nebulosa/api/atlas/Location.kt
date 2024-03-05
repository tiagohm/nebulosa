package nebulosa.api.atlas

import nebulosa.math.deg
import nebulosa.math.m
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid

data class Location(
    val latitude: Double = 0.0, // deg.
    val longitude: Double = 0.0, // deg.
    val elevation: Double = 0.0, // m.
    val offsetInMinutes: Int = 0,
) {

    fun geographicPosition(): GeographicPosition {
        return Geoid.IERS2010.lonLat(longitude.deg, latitude.deg, elevation.m)
    }
}
