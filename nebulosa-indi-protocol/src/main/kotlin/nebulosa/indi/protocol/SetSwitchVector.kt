package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class SetSwitchVector : SetVector<OneSwitch>(), SwitchVector<OneSwitch> {

    override fun toXML() =
        """<setSwitchVector device="$device" name="$name" state="$state" timeout="$timeout" timestamp="$timestamp" message="$message">${elements.toXML()}</setSwitchVector>"""
}
