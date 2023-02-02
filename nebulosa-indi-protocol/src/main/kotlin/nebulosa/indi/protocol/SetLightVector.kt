package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class SetLightVector : SetVector<OneLight>(), LightVector<OneLight> {

    override fun toXML() =
        """<setLightVector device="$device" name="$name" state="$state" timestamp="$timestamp" message="$message">${elements.toXML()}</setLightVector>"""
}
