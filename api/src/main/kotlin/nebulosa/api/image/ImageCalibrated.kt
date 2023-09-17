package nebulosa.api.image

import nebulosa.math.AngleFormatter
import nebulosa.platesolving.Calibration

data class ImageCalibrated(
    val orientation: Double = 0.0,
    val scale: Double = 0.0,
    val rightAscensionJ2000: String = "",
    val declinationJ2000: String = "",
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
