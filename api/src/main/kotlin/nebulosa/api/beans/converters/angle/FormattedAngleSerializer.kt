package nebulosa.api.beans.converters.angle

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.math.AngleFormatter
import nebulosa.math.format

abstract class FormattedAngleSerializer(private val formatter: AngleFormatter) : StdSerializer<Double>(Double::class.java) {

    override fun serialize(value: Double?, gen: JsonGenerator, provider: SerializerProvider) {
        if (value == null) gen.writeNull()
        else gen.writeString(value.format(formatter))
    }
}
