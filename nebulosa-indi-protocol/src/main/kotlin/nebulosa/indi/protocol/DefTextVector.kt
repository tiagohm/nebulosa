package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class DefTextVector : DefVector<DefText>(), TextVector<DefText> {

    override fun toXML() =
        """<defTextVector device="$device" name="$name" label="$label" group="$group" state="$state" perm="$perm" timeout="$timeout" timestamp="$timestamp" message="$message">${elements.toXML()}</defTextVector>"""
}
