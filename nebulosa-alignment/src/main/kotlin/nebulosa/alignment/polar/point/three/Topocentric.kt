package nebulosa.alignment.polar.point.three

import nebulosa.math.Angle

data class Topocentric(
    @JvmField val azimuth: Angle, @JvmField val altitude: Angle,
    @JvmField val longitude: Angle, @JvmField val latitude: Angle,
)
