package nebulosa.api.converters.distance

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.NumberSerializers
import nebulosa.math.toMeters

class MetersSerializer : NumberSerializers.DoubleSerializer(Double::class.java) {

    override fun serialize(value: Any?, gen: JsonGenerator, provider: SerializerProvider) {
        super.serialize((value as? Double)?.toMeters, gen, provider)
    }
}
