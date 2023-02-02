package nebulosa.indi.protocol

sealed class DefVector<E : DefElement<*>> : INDIProtocol(), Vector<E> {

    override val elements: MutableList<E> = ArrayList(0)

    @JvmField var group = ""

    @JvmField var label = ""

    @JvmField var perm = PropertyPermission.RW

    override var state = PropertyState.IDLE

    @JvmField var timeout = 0.0

    override fun get(name: String) = elements.firstOrNull { it.name == name }

    inline val isReadOnly get() = perm == PropertyPermission.RO

    inline val isNotReadOnly get() = !isReadOnly

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefVector<*>) return false
        if (!super.equals(other)) return false

        if (elements != other.elements) return false
        if (group != other.group) return false
        if (label != other.label) return false
        if (perm != other.perm) return false
        if (state != other.state) return false
        if (timeout != other.timeout) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + elements.hashCode()
        result = 31 * result + group.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + perm.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + timeout.hashCode()
        return result
    }
}
