package nebulosa.api.beans.converters.time

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.convert.DurationUnit
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class DurationSerializer(private val unit: ChronoUnit?) : StdSerializer<Duration>(Duration::class.java), ContextualSerializer {

    @Autowired
    constructor() : this(null)

    override fun serialize(duration: Duration?, gen: JsonGenerator, provider: SerializerProvider) {
        if (duration == null) gen.writeNull()
        else if (unit != null) gen.writeNumber(duration.toNanos() / unit.duration.toNanos())
        else gen.writeNumber(duration.toNanos() / 1000)
    }

    override fun createContextual(provider: SerializerProvider, property: BeanProperty): JsonSerializer<*> {
        val unit = property.getAnnotation(DurationUnit::class.java)?.value ?: return this
        return SERIALIZERS.getOrPut(unit) { DurationSerializer(unit) }

    }

    companion object {

        @JvmStatic private val SERIALIZERS = mutableMapOf<ChronoUnit, DurationSerializer>()
    }
}
