package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class DefLightVector : DefVector<DefLight>(), LightVector<DefLight> {

    override fun toXML() =
        """<defLightVector device="$device" name="$name" label="$label" group="$group" state="$state" timestamp="$timestamp" message="$message">${elements.toXML()}</defLightVector>"""
}
