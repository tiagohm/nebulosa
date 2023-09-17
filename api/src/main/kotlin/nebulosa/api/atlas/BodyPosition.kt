package nebulosa.api.atlas

import nebulosa.constants.AU_KM
import nebulosa.constants.SPEED_OF_LIGHT
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.AngleFormatter
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject

data class BodyPosition(
    val rightAscensionJ2000: String,
    val declinationJ2000: String,
    val rightAscension: String,
    val declination: String,
    val azimuth: String,
    val altitude: String,
    val magnitude: Double,
    val constellation: Constellation,
    val distance: Double,
    val distanceUnit: String,
    val illuminated: Double,
    val elongation: Double,
    val leading: Boolean, // true = rises and sets BEFORE Sun.
) {

    companion object {

        @JvmStatic
        fun of(element: HorizonsElement): BodyPosition {
            val lightTime = element.asDouble(HorizonsQuantity.ONE_WAY_LIGHT_TIME)
            var distance = lightTime * (SPEED_OF_LIGHT * 0.06) // km
            var distanceUnit = "km"

            if (distance <= 0.0) {
                distance = 0.0
            } else if (distance >= AU_KM) {
                distance /= AU_KM
                distanceUnit = "AU"
            }

            return BodyPosition(
                element.asDouble(HorizonsQuantity.ASTROMETRIC_RA).deg.format(AngleFormatter.HMS),
                element.asDouble(HorizonsQuantity.ASTROMETRIC_DEC).deg.format(AngleFormatter.SIGNED_DMS),
                element.asDouble(HorizonsQuantity.APPARENT_RA).deg.format(AngleFormatter.HMS),
                element.asDouble(HorizonsQuantity.APPARENT_DEC).deg.format(AngleFormatter.SIGNED_DMS),
                element.asDouble(HorizonsQuantity.APPARENT_AZ).deg.format(AngleFormatter.DMS),
                element.asDouble(HorizonsQuantity.APPARENT_ALT).deg.format(AngleFormatter.SIGNED_DMS),
                element.asDouble(HorizonsQuantity.VISUAL_MAGNITUDE, SkyObject.UNKNOWN_MAGNITUDE),
                element.asEnum(HorizonsQuantity.CONSTELLATION, Constellation.AND),
                distance, distanceUnit,
                element.asDouble(HorizonsQuantity.ILLUMINATED_FRACTION),
                element.asDouble(HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE),
                element.asString(HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE, index = 1) == "/L",
            )
        }
    }
}
