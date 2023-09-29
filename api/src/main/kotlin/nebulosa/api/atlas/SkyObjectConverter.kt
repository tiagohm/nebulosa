package nebulosa.api.atlas

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.json.modules.ToJson
import nebulosa.skycatalog.OrientedObject
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SpectralObject
import org.springframework.stereotype.Component

@Component
class SkyObjectConverter : ToJson<SkyObject> {

    override val type = SkyObject::class.java

    override fun serialize(value: SkyObject, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeNumberField("id", value.id)
        gen.writeStringField("name", value.name)
        gen.writeNumberField("magnitude", value.magnitude)
        gen.writeNumberField("rightAscensionJ2000", value.rightAscensionJ2000.value)
        gen.writeNumberField("declinationJ2000", value.declinationJ2000.value)
        gen.writeStringField("type", value.type.name)
        gen.writeNumberField("pmRA", value.pmRA.value)
        gen.writeNumberField("pmDEC", value.pmDEC.value)
        gen.writeNumberField("parallax", value.parallax.value)
        gen.writeNumberField("radialVelocity", value.radialVelocity.value)
        gen.writeNumberField("redshift", value.redshift)
        // gen.writeNumberField("distance", value.distance)
        gen.writeStringField("constellation", value.constellation.name)

        if (value is SpectralObject) {
            gen.writeStringField("spType", value.spType)
        } else if (value is OrientedObject) {
            gen.writeNumberField("majorAxis", value.majorAxis.value)
            gen.writeNumberField("minorAxis", value.minorAxis.value)
            gen.writeNumberField("orientation", value.orientation.value)
        }

        gen.writeEndObject()
    }
}
