package nebulosa.api.beans.converters.angle

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.NumberSerializers
import nebulosa.math.toDegrees

class DegreesSerializer : NumberSerializers.DoubleSerializer(Double::class.java) {

    override fun serialize(value: Any?, gen: JsonGenerator, provider: SerializerProvider) {
        super.serialize((value as? Double)?.toDegrees, gen, provider)
    }
}
