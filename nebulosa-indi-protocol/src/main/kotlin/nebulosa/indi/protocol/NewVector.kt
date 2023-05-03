package nebulosa.indi.protocol

sealed class NewVector<E : OneElement<*>> : INDIProtocol(), Vector<E> {

    override val elements = ArrayList<E>(0)

    override val state = PropertyState.BUSY

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NewVector<*>) return false
        if (!super.equals(other)) return false

        if (elements != other.elements) return false
        return state == other.state
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + elements.hashCode()
        result = 31 * result + state.hashCode()
        return result
    }
}
