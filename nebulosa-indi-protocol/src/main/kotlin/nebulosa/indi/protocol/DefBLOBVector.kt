package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class DefBLOBVector : DefVector<DefBLOB>(), BLOBVector<DefBLOB> {

    override fun toXML() =
        """<defBLOBVector device="$device" name="$name" label="$label" group="$group" state="$state" perm="$perm" timeout="$timeout" timestamp="$timestamp" message="$message">${elements.toXML()}</defBLOBVector>"""
}
