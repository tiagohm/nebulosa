package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class NewSwitchVector : NewVector<OneSwitch>(), SwitchVector<OneSwitch> {

    override fun toXML() = """<newSwitchVector device="$device" name="$name" timestamp="$timestamp">${elements.toXML()}</newSwitchVector>"""
}
