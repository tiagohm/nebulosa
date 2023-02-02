package nebulosa.indi.protocol

import nebulosa.indi.protocol.Vector.Companion.toXML

@Suppress("CanSealedSubClassBeObject")
class DefNumberVector : DefVector<DefNumber>(), NumberVector<DefNumber> {

    override fun toXML() =
        """<defNumberVector device="$device" name="$name" label="$label" group="$group" state="$state" perm="$perm" timeout="$timeout" timestamp="$timestamp" message="$message">${elements.toXML()}</defNumberVector>"""
}
