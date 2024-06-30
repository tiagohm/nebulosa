package nebulosa.api.beans.converters.angle

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import nebulosa.math.Angle

abstract class FormattedAngleDeserializer(
    private val isHours: Boolean = false,
    private val decimalIsHours: Boolean = isHours,
    private val defaultValue: Angle = Double.NaN,
) : StdDeserializer<Double>(Double::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Double {
        return Angle(p.text, isHours, decimalIsHours, defaultValue)
    }
}
