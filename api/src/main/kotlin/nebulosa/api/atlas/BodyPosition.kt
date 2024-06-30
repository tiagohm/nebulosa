package nebulosa.api.atlas

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.beans.converters.angle.AzimuthSerializer
import nebulosa.api.beans.converters.angle.DeclinationSerializer
import nebulosa.api.beans.converters.angle.DegreesSerializer
import nebulosa.api.beans.converters.angle.RightAscensionSerializer
import nebulosa.constants.AU_KM
import nebulosa.constants.SPEED_OF_LIGHT
import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObject

data class BodyPosition(
    @field:JsonSerialize(using = RightAscensionSerializer::class) val rightAscensionJ2000: Angle,
    @field:JsonSerialize(using = DeclinationSerializer::class) val declinationJ2000: Angle,
    @field:JsonSerialize(using = RightAscensionSerializer::class) val rightAscension: Angle,
    @field:JsonSerialize(using = DeclinationSerializer::class) val declination: Angle,
    @field:JsonSerialize(using = AzimuthSerializer::class) val azimuth: Angle,
    @field:JsonSerialize(using = DeclinationSerializer::class) val altitude: Angle,
    val magnitude: Double,
    val constellation: Constellation,
    val distance: Double,
    val distanceUnit: String,
    val illuminated: Double,
    @field:JsonSerialize(using = DegreesSerializer::class) val elongation: Angle,
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

            val rightAscension = element.asDouble(HorizonsQuantity.APPARENT_RA).deg
            val declination = element.asDouble(HorizonsQuantity.APPARENT_DEC).deg

            return BodyPosition(
                element.asDouble(HorizonsQuantity.ASTROMETRIC_RA).deg,
                element.asDouble(HorizonsQuantity.ASTROMETRIC_DEC).deg,
                rightAscension, declination,
                element.asDoubleOrNull(HorizonsQuantity.APPARENT_AZ)?.deg ?: element.asDouble(HorizonsQuantity.APPARENT_REFRACTED_AZ).deg,
                element.asDoubleOrNull(HorizonsQuantity.APPARENT_ALT)?.deg ?: element.asDouble(HorizonsQuantity.APPARENT_REFRACTED_ALT).deg,
                element.asDouble(HorizonsQuantity.VISUAL_MAGNITUDE, SkyObject.UNKNOWN_MAGNITUDE),
                element.asEnum(HorizonsQuantity.CONSTELLATION, Constellation.AND),
                distance, distanceUnit,
                element.asDouble(HorizonsQuantity.ILLUMINATED_FRACTION),
                element.asDouble(HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE).deg,
                element.asStringOrNull(HorizonsQuantity.SUN_OBSERVER_TARGET_ELONGATION_ANGLE, index = 1) == "/L",
            )
        }
    }
}
