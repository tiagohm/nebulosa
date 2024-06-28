package nebulosa.api.beans.converters.distance

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import nebulosa.math.m

class MetersDeserializer : NumberDeserializers.DoubleDeserializer(Double::class.java, 0.0) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Double {
        return super.deserialize(p, ctxt).m
    }
}
