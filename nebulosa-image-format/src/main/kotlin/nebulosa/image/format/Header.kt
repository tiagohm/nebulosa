package nebulosa.image.format

import java.util.function.Predicate

interface Header : ReadableHeader, WritableHeader, Cloneable {

    public override fun clone(): Header

    data object Empty : Header, MutableIterator<HeaderCard> {

        override fun clone() = this

        override fun contains(element: HeaderCard) = false

        override val size = 0

        override fun containsAll(elements: Collection<HeaderCard>) = false

        override fun isEmpty() = true

        override fun iterator() = this

        override fun clear() = Unit

        override fun remove(element: HeaderCard) = false

        override fun removeAll(elements: Collection<HeaderCard>) = false

        override fun retainAll(elements: Collection<HeaderCard>) = false

        override fun add(key: String, value: Boolean, comment: String) = Unit

        override fun add(key: String, value: Int, comment: String) = Unit

        override fun add(key: String, value: Double, comment: String) = Unit

        override fun add(key: String, value: String, comment: String) = Unit

        override fun add(element: HeaderCard) = false

        override fun addAll(elements: Collection<HeaderCard>) = false

        override fun delete(key: String) = false

        override fun hasNext() = false

        override fun next() = TODO("Unsupported operation")

        override fun remove() = Unit

        override fun removeIf(filter: Predicate<in HeaderCard>) = false
    }
}
