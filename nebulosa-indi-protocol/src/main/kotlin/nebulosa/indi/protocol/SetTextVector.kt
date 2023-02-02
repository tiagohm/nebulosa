package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class SetTextVector : SetVector<OneText>(), TextVector<OneText> {

    override fun toXML() =
        """<setTextVector device="$device" name="$name" state="$state" timeout="$timeout" timestamp="$timestamp" message="$message">${elements.toXML()}</setTextVector>"""
}
