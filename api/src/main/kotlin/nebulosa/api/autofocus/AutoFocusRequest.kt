package nebulosa.api.autofocus

import nebulosa.api.cameras.CameraStartCaptureRequest
import nebulosa.api.focusers.BacklashCompensation
import nebulosa.api.stardetection.StarDetectionOptions

data class AutoFocusRequest(
    @JvmField val fittingMode: AutoFocusFittingMode = AutoFocusFittingMode.HYPERBOLIC,
    @JvmField val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @JvmField val rSquaredThreshold: Double = 0.7,
    @JvmField val backlashCompensation: BacklashCompensation = BacklashCompensation.EMPTY,
    @JvmField val initialOffsetSteps: Int = 4,
    @JvmField val stepSize: Int = 50,
    @JvmField val totalNumberOfAttempts: Int = 1,
    @JvmField val starDetector: StarDetectionOptions = StarDetectionOptions.EMPTY,
)
