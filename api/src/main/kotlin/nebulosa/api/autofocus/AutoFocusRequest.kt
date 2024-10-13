package nebulosa.api.autofocus

import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.focusers.BacklashCompensation
import nebulosa.api.stardetector.StarDetectionRequest
import nebulosa.api.validators.Validatable
import nebulosa.api.validators.max
import nebulosa.api.validators.positive
import nebulosa.api.validators.range
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
