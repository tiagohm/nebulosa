package nebulosa.indi.protocol

class OneBLOB : OneElement<String>(), BLOBElement {

    @JvmField var format = ""

    @JvmField var size = ""

    override var value = ""

    override fun toXML() =
        """<oneBLOB name="$name" size="$size" format="$format">$value</oneBLOB>"""
}

