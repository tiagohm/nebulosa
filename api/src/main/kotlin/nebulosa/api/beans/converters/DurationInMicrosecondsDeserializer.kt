package nebulosa.api.beans.converters

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class DurationInMicrosecondsDeserializer : StdDeserializer<Duration>(Duration::class.java) {

    private val numberDeserializer = NumberDeserializers.LongDeserializer(Long::class.java, 0L)

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Duration {
        return Duration.of(numberDeserializer.deserialize(p, ctxt), ChronoUnit.MICROS)
    }
}
