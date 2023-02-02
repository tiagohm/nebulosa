package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class NewNumberVector : NewVector<OneNumber>(), MinMaxVector<OneNumber> {

    override fun toXML() = """<newNumberVector device="$device" name="$name" timestamp="$timestamp">${elements.toXML()}</newNumberVector>"""
}
