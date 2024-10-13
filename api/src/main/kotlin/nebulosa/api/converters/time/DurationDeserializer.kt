package nebulosa.api.converters.time

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.Duration
import java.time.temporal.ChronoUnit

class DurationDeserializer(private val unit: ChronoUnit? = null) : StdDeserializer<Duration>(Duration::class.java), ContextualDeserializer {

    private val numberDeserializer by lazy { NumberDeserializers.LongDeserializer(Long::class.java, 0L) }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Duration? {
        return if (unit == null) p.text?.ifBlank { null }?.toLongOrNull()
            ?.let { Duration.of(it, ChronoUnit.MICROS) }
        else Duration.of(numberDeserializer.deserialize(p, ctxt), unit)
    }

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {
        val unit = property.getAnnotation(DurationUnit::class.java)?.unit ?: return this
        return DESERIALIZERS.getOrPut(unit) { DurationDeserializer(unit) }
    }

    companion object {

        @JvmStatic private val DESERIALIZERS = mutableMapOf<ChronoUnit, DurationDeserializer>()
    }
}
