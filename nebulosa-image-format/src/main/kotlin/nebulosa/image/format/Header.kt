package nebulosa.image.format

import nebulosa.io.SeekableSource
import okio.Sink

interface Header : ReadableHeader, WritableHeader, Cloneable {

    public override fun clone(): Header

    data object Empty : Header, Iterator<HeaderCard> {

        override fun clone() = this

        override fun contains(element: HeaderCard) = false

        override val size = 0

        override fun containsAll(elements: Collection<HeaderCard>) = false

        override fun isEmpty() = true

        override fun iterator() = this

        override fun write(sink: Sink) = Unit

        override fun clear() = Unit

        override fun add(key: String, value: Boolean, comment: String) = Unit

        override fun add(key: String, value: Int, comment: String) = Unit

        override fun add(key: String, value: Double, comment: String) = Unit

        override fun add(key: String, value: String, comment: String) = Unit

        override fun delete(key: String) = false

        override fun read(source: SeekableSource) = Unit

        override fun hasNext() = false

        override fun next() = TODO("Unsupported operation")
    }
}
