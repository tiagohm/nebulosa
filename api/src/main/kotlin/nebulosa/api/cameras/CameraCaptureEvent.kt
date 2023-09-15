package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera

sealed interface CameraCaptureEvent {

    val camera: Camera

    companion object {

        const val WAIT_PROGRESS = "waitProgress"
        const val WAIT_REMAINING_TIME = "waitRemainingTime"
        const val WAIT_TIME = "waitTime"

        const val EXPOSURE_AMOUNT = "exposureAmount"
        const val EXPOSURE_COUNT = "exposureCount"
        const val EXPOSURE_TIME = "exposureTime"
        const val EXPOSURE_REMAINING_TIME = "exposureRemainingTime"
        const val EXPOSURE_PROGRESS = "exposureProgress"

        const val CAPTURE_TIME = "captureTime"
        const val CAPTURE_REMAINING_TIME = "captureRemainingTime"
        const val CAPTURE_PROGRESS = "captureProgress"
        const val CAPTURE_IN_LOOP = "captureInLoop"
        const val CAPTURE_IS_WAITING = "captureIsWaiting"
        const val CAPTURE_ELAPSED_TIME = "captureElapsedTime"
    }
}
