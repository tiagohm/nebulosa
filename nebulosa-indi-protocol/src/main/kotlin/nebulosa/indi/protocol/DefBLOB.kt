package nebulosa.indi.protocol

class DefBLOB : DefElement<String>(), BLOBElement {

    override var value = ""

    override fun toXML() = """<defBLOB name="$name" label="$label"></defBLOB>"""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefBLOB) return false
        if (!super.equals(other)) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}
