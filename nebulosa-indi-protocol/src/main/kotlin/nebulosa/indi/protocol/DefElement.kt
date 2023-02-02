package nebulosa.indi.protocol

sealed class DefElement<T> : INDIProtocol(), Element<T> {

    @JvmField var label = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefElement<*>) return false
        if (!super.equals(other)) return false

        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}
