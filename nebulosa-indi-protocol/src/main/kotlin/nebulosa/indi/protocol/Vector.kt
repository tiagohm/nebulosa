package nebulosa.indi.protocol

sealed interface Vector<E : Element<*>> : INDIProtocol, List<E> {

    var state: PropertyState

    val type: PropertyType

    val elements: MutableList<E>

    override val size
        get() = elements.size

    override fun contains(element: E) = element in elements

    override fun containsAll(elements: Collection<E>) = elements.containsAll(elements)

    override fun get(index: Int) = elements[index]

    operator fun get(name: String) = elements.firstOrNull { it.name == name }

    operator fun contains(name: String) = elements.any { it.name == name }

    override fun indexOf(element: E) = elements.indexOf(element)

    override fun isEmpty() = elements.isEmpty()

    override fun iterator() = elements.iterator()

    override fun lastIndexOf(element: E) = elements.lastIndexOf(element)

    override fun listIterator() = elements.listIterator()

    override fun listIterator(index: Int) = elements.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = elements.subList(fromIndex, toIndex)

    companion object {

        inline val Vector<*>.isIdle
            get() = state == PropertyState.IDLE

        inline val Vector<*>.isBusy
            get() = state == PropertyState.BUSY

        inline val Vector<*>.isAlert
            get() = state == PropertyState.ALERT

        inline val Vector<*>.isOk
            get() = state == PropertyState.OK
    }
}
