package nebulosa.api.sequencer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.json.modules.ToJson
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
class SequenceJobConverter : ToJson<SequenceJob> {

    override val type = SequenceJob::class.java

    override fun serialize(value: SequenceJob, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectField("devices", value.devices)
        gen.writeNumberField("jobId", value.jobId)
        gen.writeNumberField("startTime", value.startTime?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: 0L)
        gen.writeNumberField("endTime", value.endTime?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: 0L)
        gen.writeEndObject()
    }
}
