package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import org.springframework.batch.item.ExecutionContext

sealed interface CameraCaptureEvent {

    val camera: Camera

    val executionContext: ExecutionContext

    val waitProgress
        get() = executionContext.getDouble(WAIT_PROGRESS, 0.0)

    val waitRemainingTime
        get() = executionContext.getLong(WAIT_REMAINING_TIME, 0L)

    val waitTime
        get() = executionContext.getLong(WAIT_TIME, 0L)

    val exposureAmount
        get() = executionContext.getInt(EXPOSURE_AMOUNT, 0)

    val exposureCount
        get() = executionContext.getInt(EXPOSURE_COUNT, 0)

    val exposureTime
        get() = executionContext.getLong(EXPOSURE_TIME, 0L)

    val exposureRemainingTime
        get() = executionContext.getLong(EXPOSURE_REMAINING_TIME, 0L)

    val exposureProgress
        get() = executionContext.getDouble(EXPOSURE_PROGRESS, 0.0)

    val captureTime
        get() = executionContext.getLong(CAPTURE_TIME, 0L)

    val captureRemainingTime
        get() = executionContext.getLong(CAPTURE_REMAINING_TIME, 0L)

    val captureProgress
        get() = executionContext.getDouble(CAPTURE_PROGRESS, 0.0)

    val captureInLoop
        get() = executionContext.get(CAPTURE_IN_LOOP) == true

    val captureIsWaiting
        get() = executionContext.get(CAPTURE_IS_WAITING) == true

    val captureElapsedTime
        get() = executionContext.getLong(CAPTURE_ELAPSED_TIME, 0L)

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
