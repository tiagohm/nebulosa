package nebulosa.api.beans.converters.time

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.convert.DurationUnit
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class DurationDeserializer(private val unit: ChronoUnit?) : StdDeserializer<Duration>(Duration::class.java), ContextualDeserializer {

    private val numberDeserializer by lazy { NumberDeserializers.LongDeserializer(Long::class.java, 0L) }

    @Autowired
    constructor() : this(null)

    @Autowired @Lazy private lateinit var converter: StringToDurationConverter

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Duration? {
        return if (unit == null) converter.convert(p.text)
        else Duration.of(numberDeserializer.deserialize(p, ctxt), unit)
    }

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {
        val unit = property.getAnnotation(DurationUnit::class.java)?.value ?: return this
        return DESERIALIZERS.getOrPut(unit) { DurationDeserializer(unit) }
    }

    companion object {

        @JvmStatic private val DESERIALIZERS = mutableMapOf<ChronoUnit, DurationDeserializer>()
    }
}
