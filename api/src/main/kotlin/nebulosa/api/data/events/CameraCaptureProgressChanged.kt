package nebulosa.api.data.events

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.api.services.CameraExposureTask
import nebulosa.indi.device.camera.CameraEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class CameraCaptureProgressChanged(
    override val task: CameraExposureTask,
) : TaskEvent, CameraEvent {

    override val device
        get() = task.camera

    @Component
    @Qualifier("serializer")
    class Serializer : StdSerializer<CameraCaptureProgressChanged>(CameraCaptureProgressChanged::class.java) {

        override fun serialize(
            event: CameraCaptureProgressChanged,
            gen: JsonGenerator,
            provider: SerializerProvider,
        ) {
            gen.writeStartObject()
            gen.writeStringField("camera", event.device.name)
            gen.writeNumberField("remainingAmount", event.task.remainingAmount)
            gen.writeNumberField("frameRemainingTime", event.task.frameRemainingTime)
            gen.writeNumberField("frameProgress", event.task.frameProgress)
            gen.writeNumberField("totalAmount", event.task.amount)
            gen.writeNumberField("totalRemainingTime", event.task.totalRemainingTime)
            gen.writeNumberField("totalProgress", event.task.totalProgress)
            gen.writeNumberField("totalExposureTime", event.task.totalExposureTime)
            gen.writeBooleanField("indeterminate", event.task.indeterminate)
            gen.writeEndObject()
        }
    }
}
