package nebulosa.image.format

import java.io.Serializable
import java.util.*

abstract class AbstractHeader protected constructor(@JvmField protected val cards: LinkedList<HeaderCard>) :
    Header, Collection<HeaderCard> by cards, Serializable {

    constructor() : this(LinkedList<HeaderCard>())

    constructor(cards: Collection<HeaderCard>) : this(LinkedList(cards))

    abstract fun readOnly(): Header

    override fun clear() {
        cards.clear()
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

    override fun hashCode(): Int {
        return cards.hashCode()
    }

    override fun toString(): String {
        return "Header(cards=$cards)"
    }
}
