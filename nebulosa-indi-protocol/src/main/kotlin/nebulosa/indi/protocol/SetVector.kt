package nebulosa.indi.protocol

sealed class SetVector<E : OneElement<*>> : INDIProtocol(), Vector<E> {

    override var elements = ArrayList<E>(0)

    override var state = PropertyState.IDLE

    @JvmField var timeout = 0.0

    override fun get(name: String) = elements.firstOrNull { it.name == name }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SetVector<*>) return false
        if (!super.equals(other)) return false

        if (elements != other.elements) return false
        if (state != other.state) return false
        if (timeout != other.timeout) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + elements.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + timeout.hashCode()
        return result
    }
}
