package nebulosa.api.cameras

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_ELAPSED_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_IN_LOOP
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_IS_WAITING
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_PROGRESS
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_REMAINING_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.CAPTURE_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_AMOUNT
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_COUNT
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_PROGRESS
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_REMAINING_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.EXPOSURE_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.WAIT_PROGRESS
import nebulosa.api.cameras.CameraCaptureEvent.Companion.WAIT_REMAINING_TIME
import nebulosa.api.cameras.CameraCaptureEvent.Companion.WAIT_TIME
import nebulosa.json.ToJson
import org.springframework.stereotype.Component

@Component
class CameraCaptureEventConverter : ToJson<CameraCaptureEvent> {

    override val type = CameraCaptureEvent::class.java

    override fun serialize(value: CameraCaptureEvent, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()

        gen.writeObjectField("camera", value.camera)

        gen.writeNumberField(WAIT_PROGRESS, value.waitProgress)
        gen.writeNumberField(WAIT_REMAINING_TIME, value.waitRemainingTime)
        gen.writeNumberField(WAIT_TIME, value.waitTime)

        gen.writeNumberField(EXPOSURE_AMOUNT, value.exposureAmount)
        gen.writeNumberField(EXPOSURE_COUNT, value.exposureCount)
        gen.writeNumberField(EXPOSURE_TIME, value.exposureTime)
        gen.writeNumberField(EXPOSURE_REMAINING_TIME, value.exposureRemainingTime)
        gen.writeNumberField(EXPOSURE_PROGRESS, value.exposureProgress)

        gen.writeNumberField(CAPTURE_TIME, value.captureTime)
        gen.writeNumberField(CAPTURE_REMAINING_TIME, value.captureRemainingTime)
        gen.writeNumberField(CAPTURE_PROGRESS, value.captureProgress)
        gen.writeBooleanField(CAPTURE_IN_LOOP, value.captureInLoop)
        gen.writeBooleanField(CAPTURE_IS_WAITING, value.captureIsWaiting)
        gen.writeNumberField(CAPTURE_ELAPSED_TIME, value.captureElapsedTime)

        if (value is CameraExposureFinished && value.savePath != null) {
            gen.writeObjectField("savePath", value.savePath)
        }

        gen.writeEndObject()
    }
}
