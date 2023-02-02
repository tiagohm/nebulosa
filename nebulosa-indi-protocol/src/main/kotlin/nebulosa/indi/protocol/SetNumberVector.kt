package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class SetNumberVector : SetVector<OneNumber>(), NumberVector<OneNumber> {

    override fun toXML() =
        """<setNumberVector device="$device" name="$name" state="$state" timeout="$timeout" timestamp="$timestamp" message="$message">${elements.toXML()}</setNumberVector>"""
}
