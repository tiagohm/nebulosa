package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class NewBLOBVector : NewVector<OneBLOB>(), BLOBVector<OneBLOB> {

    override fun toXML() = """<newBLOBVector device="$device" name="$name" timestamp="$timestamp">${elements.toXML()}</newBLOBVector>"""
}
