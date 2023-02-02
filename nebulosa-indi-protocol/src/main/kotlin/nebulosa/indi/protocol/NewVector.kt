package nebulosa.indi.protocol

sealed class NewVector<E : OneElement<*>> : INDIProtocol(), Vector<E> {

    override var elements = ArrayList<E>(0)

    override var state = PropertyState.BUSY

    override fun get(name: String) = elements.firstOrNull { it.name == name }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NewVector<*>) return false
        if (!super.equals(other)) return false

        if (elements != other.elements) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + elements.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }
}
