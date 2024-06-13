package nebulosa.image.format

import java.io.Serializable
import java.util.*

abstract class AbstractHeader protected constructor(@JvmField protected val cards: LinkedList<HeaderCard>) : Header, Serializable {

    constructor() : this(LinkedList<HeaderCard>())

    constructor(cards: Collection<HeaderCard>) : this(LinkedList(cards))

    abstract fun readOnly(): Header

    override fun add(element: HeaderCard): Boolean {
        return if (!element.isKeyValuePair) {
            cards.add(element)
        } else {
            val index = cards.indexOfFirst { it.key == element.key }

            if (index >= 0) {
                cards[index] = element
                true
            } else {
                cards.add(element)
            }
        }
    }

    override val size
        get() = cards.size

    override fun contains(element: HeaderCard): Boolean {
        return element in cards
    }

    override fun containsAll(elements: Collection<HeaderCard>): Boolean {
        return cards.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return cards.isEmpty()
    }

    override fun iterator(): MutableIterator<HeaderCard> {
        return cards.iterator()
    }

    override fun clear() {
        cards.clear()
    }

    override fun remove(element: HeaderCard): Boolean {
        return cards.remove(element)
    }

    override fun removeAll(elements: Collection<HeaderCard>): Boolean {
        return cards.removeAll(elements.toSet())
    }

    override fun retainAll(elements: Collection<HeaderCard>): Boolean {
        return cards.retainAll(elements.toSet())
    }

    override fun delete(key: String): Boolean {
        return cards.removeIf { it.key == key }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractHeader) return false

        if (cards != other.cards) return false

        return true
    }

    override fun hashCode() = cards.hashCode()

    override fun toString() = "${javaClass.simpleName}(cards=$cards)"
}
