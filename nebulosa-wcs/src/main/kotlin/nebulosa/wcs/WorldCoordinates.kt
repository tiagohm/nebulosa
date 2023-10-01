package nebulosa.wcs

import nebulosa.math.Angle

data class WorldCoordinates(
    val rightAscension: Angle,
    val declination: Angle,
    val phi: Angle,
    val theta: Angle,
)
