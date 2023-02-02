package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class NewLightVector : NewVector<OneLight>(), LightVector<OneLight> {

    override fun toXML() = """<newLightVector device="$device" name="$name" timestamp="$timestamp">${elements.toXML()}</newLightVector>"""
}
