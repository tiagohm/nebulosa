package nebulosa.api.cameras

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
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
import nebulosa.api.sequencer.SequenceJobEvent
import nebulosa.api.sequencer.SequenceStepEvent
import org.springframework.stereotype.Component

@Component
class CameraCaptureEventSerializer : StdSerializer<CameraCaptureEvent>(CameraCaptureEvent::class.java) {

    override fun serialize(value: CameraCaptureEvent, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()

        gen.writeStringField("eventName", value.eventName)
        gen.writeObjectField("camera", value.camera)

        val executionContext = when (value) {
            is SequenceStepEvent -> value.stepExecution.executionContext
            is SequenceJobEvent -> value.jobExecution.executionContext
            else -> null
        }

        if (executionContext != null) {
            gen.writeNumberField(WAIT_PROGRESS, executionContext.getDouble(WAIT_PROGRESS, 0.0))
            gen.writeNumberField(WAIT_REMAINING_TIME, executionContext.getLong(WAIT_REMAINING_TIME, 0L))
            gen.writeNumberField(WAIT_TIME, executionContext.getLong(WAIT_TIME, 0L))

            gen.writeNumberField(EXPOSURE_AMOUNT, executionContext.getInt(EXPOSURE_AMOUNT, 0))
            gen.writeNumberField(EXPOSURE_COUNT, executionContext.getInt(EXPOSURE_COUNT, 0))
            gen.writeNumberField(EXPOSURE_TIME, executionContext.getLong(EXPOSURE_TIME, 0L))
            gen.writeNumberField(EXPOSURE_REMAINING_TIME, executionContext.getLong(EXPOSURE_REMAINING_TIME, 0L))
            gen.writeNumberField(EXPOSURE_PROGRESS, executionContext.getDouble(EXPOSURE_PROGRESS, 0.0))

            gen.writeNumberField(CAPTURE_TIME, executionContext.getLong(CAPTURE_TIME, 0L))
            gen.writeNumberField(CAPTURE_REMAINING_TIME, executionContext.getLong(CAPTURE_REMAINING_TIME, 0L))
            gen.writeNumberField(CAPTURE_PROGRESS, executionContext.getDouble(CAPTURE_PROGRESS, 0.0))
            gen.writeBooleanField(CAPTURE_IN_LOOP, executionContext.get(CAPTURE_IN_LOOP) == true)
            gen.writeBooleanField(CAPTURE_IS_WAITING, executionContext.get(CAPTURE_IS_WAITING) == true)
            gen.writeNumberField(CAPTURE_ELAPSED_TIME, executionContext.getLong(CAPTURE_ELAPSED_TIME, 0L))
        }

        if (value is CameraExposureFinished && value.savePath != null) {
            gen.writeObjectField("savePath", value.savePath)
        }

        gen.writeEndObject()
    }
}
