package nebulosa.indi.protocol

sealed class DefElement<T> : INDIProtocol(), Element<T> {

    var label = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefElement<*>) return false
        if (!super.equals(other)) return false
        return label == other.label
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}
