package nebulosa.api.data.events

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.api.services.GuideExposureTask
import nebulosa.indi.device.camera.CameraEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class GuideExposureFinished(
    override val task: GuideExposureTask,
) : TaskEvent, CameraEvent {

    override val device
        get() = task.camera

    @Component
    @Qualifier("serializer")
    class Serializer : StdSerializer<GuideExposureFinished>(GuideExposureFinished::class.java) {

        override fun serialize(
            event: GuideExposureFinished,
            gen: JsonGenerator,
            provider: SerializerProvider,
        ) {
            gen.writeStartObject()
            gen.writeStringField("camera", event.device.name)
            gen.writeStringField("path", "${event.task.savePath}")
            gen.writeEndObject()
        }
    }
}
