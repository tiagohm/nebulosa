package nebulosa.indi.protocol

sealed class SetVector<E : OneElement<*>> : INDIProtocol(), Vector<E> {

    override var elements = ArrayList<E>(0)

    override var state = PropertyState.IDLE

    @JvmField var timeout = 0.0

    override fun get(name: String) = elements.firstOrNull { it.name == name }
}
