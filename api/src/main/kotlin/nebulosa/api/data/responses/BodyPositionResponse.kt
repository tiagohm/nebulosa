package nebulosa.api.data.responses

import nebulosa.horizons.HorizonsElement
import nebulosa.horizons.HorizonsQuantity
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.AngleFormatter

data class BodyPositionResponse(
    val rightAscensionJ2000: String,
    val declinationJ2000: String,
    val rightAscension: String,
    val declination: String,
    val azimuth: String,
    val altitude: String,
) {

    companion object {

        @JvmStatic
        fun of(element: HorizonsElement) = BodyPositionResponse(
            element.asDouble(HorizonsQuantity.ASTROMETRIC_RA).deg.format(AngleFormatter.HMS),
            element.asDouble(HorizonsQuantity.ASTROMETRIC_DEC).deg.format(AngleFormatter.SIGNED_DMS),
            element.asDouble(HorizonsQuantity.APPARENT_RA).deg.format(AngleFormatter.HMS),
            element.asDouble(HorizonsQuantity.APPARENT_DEC).deg.format(AngleFormatter.SIGNED_DMS),
            element.asDouble(HorizonsQuantity.APPARENT_AZ).deg.format(AngleFormatter.DMS),
            element.asDouble(HorizonsQuantity.APPARENT_ALT).deg.format(AngleFormatter.SIGNED_DMS),
        )
    }
}
