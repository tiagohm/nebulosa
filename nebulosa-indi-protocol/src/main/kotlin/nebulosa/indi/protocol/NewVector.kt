package nebulosa.indi.protocol

sealed class NewVector<E : OneElement<*>> : INDIProtocol(), Vector<E> {

    override var elements = ArrayList<E>(0)

    override var state = PropertyState.BUSY

    override fun get(name: String) = elements.firstOrNull { it.name == name }
}
