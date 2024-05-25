package nebulosa.api.autofocus

import nebulosa.api.cameras.CameraStartCaptureRequest

data class AutoFocusRequest(
    @JvmField val fittingMode: AutoFocusFittingMode = AutoFocusFittingMode.HYPERBOLIC,
    @JvmField val capture: CameraStartCaptureRequest = CameraStartCaptureRequest.EMPTY,
    @JvmField val rSquaredThreshold: Double = 0.7,
    @JvmField val backlashCompensationMode: BacklashCompensationMode = BacklashCompensationMode.OVERSHOOT,
    @JvmField val backlashIn: Int = 0,
    @JvmField val backlashOut: Int = 0,
    @JvmField val initialOffsetSteps: Int = 4,
    @JvmField val stepSize: Int = 50,
    @JvmField val totalNumberOfAttempts: Int = 1,
)
