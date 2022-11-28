package nebulosa.indi.protocol

interface Vector<E : Element<*>> : List<E> {

    var state: PropertyState

    var elements: ArrayList<E>

    override val size get() = elements.size

    override fun contains(element: E) = element in elements

    override fun containsAll(elements: Collection<E>) = elements.containsAll(elements)

    override fun get(index: Int) = elements[index]

    operator fun get(name: String): E?

    override fun indexOf(element: E) = elements.indexOf(element)

    override fun isEmpty() = elements.isEmpty()

    override fun iterator() = elements.iterator()

    override fun lastIndexOf(element: E) = elements.lastIndexOf(element)

    override fun listIterator() = elements.listIterator()

    override fun listIterator(index: Int) = elements.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = elements.subList(fromIndex, toIndex)
}
