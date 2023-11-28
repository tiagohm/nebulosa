package nebulosa.fits

import nebulosa.io.SeekableSource
import java.util.*

internal class ReadOnlyHeader : Header {

    constructor() : super(LinkedList<HeaderCard>())

    constructor(cards: Collection<HeaderCard>) : super(LinkedList(cards))

    constructor(header: Header) : super(LinkedList(header.cards))

    override fun read(source: SeekableSource) = throw UnsupportedOperationException("Header is read-only")

    override fun clear() = throw UnsupportedOperationException("Header is read-only")

    override fun add(key: FitsHeader, value: Boolean) = throw UnsupportedOperationException("Header is read-only")

    override fun add(key: FitsHeader, value: Int) = throw UnsupportedOperationException("Header is read-only")

    override fun add(key: FitsHeader, value: Double) = throw UnsupportedOperationException("Header is read-only")

    override fun add(key: FitsHeader, value: String) = throw UnsupportedOperationException("Header is read-only")

    override fun add(card: HeaderCard) = throw UnsupportedOperationException("Header is read-only")

    override fun delete(key: FitsHeader) = throw UnsupportedOperationException("Header is read-only")
}
