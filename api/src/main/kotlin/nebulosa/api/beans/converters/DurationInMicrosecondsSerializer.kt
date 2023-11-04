package nebulosa.api.beans.converters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class DurationInMicrosecondsSerializer : StdSerializer<Duration>(Duration::class.java) {

    override fun serialize(value: Duration?, gen: JsonGenerator, provider: SerializerProvider) {
        value?.also { gen.writeNumber(it.toMillis()) } ?: gen.writeNull()
    }
}
