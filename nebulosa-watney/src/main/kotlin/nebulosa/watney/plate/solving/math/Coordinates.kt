package nebulosa.watney.plate.solving.math

import nebulosa.math.Angle
import kotlin.math.*

/**
 * Transforms the [ra], [dec] coords to standard coordinates
 * around the given [centerRA], [centerDEC].
 */
fun equatorialToStandardCoordinates(ra: Angle, dec: Angle, centerRA: Angle, centerDEC: Angle): DoubleArray {
    val divider = cos(centerDEC) * cos(dec) * cos(ra - centerRA) + sin(centerDEC) * sin(dec)
    val starX = cos(dec) * sin(ra - centerRA) / divider
    val starY = (sin(centerDEC) * cos(dec) * cos(ra - centerRA) - cos(centerDEC) * sin(dec)) / divider
    return doubleArrayOf(starX, starY)
}

/**
 * Transform standard coordinates to equatorial coordinates when the field center
 * equatorial coordinates are given.
 */
fun standardToEquatorialCoordinates(centerRA: Angle, centerDEC: Angle, stdX: Double, stdY: Double): DoubleArray {
    val ra = centerRA + atan2(-stdX, cos(centerDEC) - stdY * sin(centerDEC))
    val dec = asin((sin(centerDEC) + stdY * cos(centerDEC)) / sqrt(1 + stdX * stdX + stdY * stdY))
    return doubleArrayOf(ra, dec)
}
