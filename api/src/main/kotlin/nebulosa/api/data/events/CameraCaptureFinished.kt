package nebulosa.api.data.events

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.api.services.CameraExposureTask
import nebulosa.indi.device.camera.CameraEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class CameraCaptureFinished(override val task: CameraExposureTask) : TaskEvent, CameraEvent {

    override val device
        get() = task.camera

    @Component
    @Qualifier("serializer")
    class Serializer : StdSerializer<CameraCaptureFinished>(CameraCaptureFinished::class.java) {

        override fun serialize(
            event: CameraCaptureFinished,
            gen: JsonGenerator,
            provider: SerializerProvider,
        ) {
            gen.writeStartObject()
            gen.writeStringField("camera", event.device.name)
            gen.writeEndObject()
        }
    }
}
