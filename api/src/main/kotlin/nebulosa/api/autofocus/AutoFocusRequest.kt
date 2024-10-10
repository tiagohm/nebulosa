package nebulosa.api.autofocus

import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.focusers.BacklashCompensation
import nebulosa.api.javalin.Validatable
import nebulosa.api.javalin.max
import nebulosa.api.javalin.positive
import nebulosa.api.javalin.range
import nebulosa.api.stardetector.StarDetectionRequest
import nebulosa.autofocus.AutoFocusFittingMode

data class AutoFocusRequest(
    @JvmField val fittingMode: AutoFocusFittingMode = AutoFocusFittingMode.HYPERBOLIC,
    @JvmField val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @JvmField val rSquaredThreshold: Double = 0.5,
    @JvmField val backlashCompensation: BacklashCompensation = BacklashCompensation.EMPTY,
    @JvmField val initialOffsetSteps: Int = 4,
    @JvmField val stepSize: Int = 50,
    @JvmField val starDetector: StarDetectionRequest = StarDetectionRequest.EMPTY,
) : Validatable {

    override fun validate() {
        initialOffsetSteps.positive().max(1000)
        rSquaredThreshold.range(0.0, 1.0)
        stepSize.positive()
        capture.validate()
        starDetector.validate()
    }
}
