package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class NewTextVector : NewVector<OneText>(), TextVector<OneText> {

    override fun toXML() = """<newTextVector device="$device" name="$name" timestamp="$timestamp">${elements.toXML()}</newTextVector>"""
}
