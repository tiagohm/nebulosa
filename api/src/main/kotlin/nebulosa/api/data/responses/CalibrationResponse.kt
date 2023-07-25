package nebulosa.api.data.responses

import nebulosa.math.AngleFormatter
import nebulosa.platesolving.Calibration

data class CalibrationResponse(
    val orientation: Double = 0.0,
    val scale: Double = 0.0,
    val rightAscension: String = "",
    val declination: String = "",
    val width: Double = 0.0,
    val height: Double = 0.0,
    val radius: Double = 0.0,
) {

    constructor(calibration: Calibration) : this(
        calibration.orientation.degrees,
        calibration.scale.arcsec,
        calibration.rightAscension.format(AngleFormatter.HMS),
        calibration.declination.format(AngleFormatter.SIGNED_DMS),
        calibration.width.arcmin, calibration.height.arcmin,
        calibration.radius.degrees,
    )
}
