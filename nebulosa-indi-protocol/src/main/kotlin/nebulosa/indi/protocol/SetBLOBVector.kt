package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class SetBLOBVector : SetVector<OneBLOB>(), BLOBVector<OneBLOB> {

    override fun toXML() =
        """<setBLOBVector device="$device" name="$name" state="$state" timeout="$timeout" timestamp="$timestamp" message="$message">${elements.toXML()}</setBLOBVector>"""
}
