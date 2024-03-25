package nebulosa.xisf

import nebulosa.image.format.AbstractHeader
import nebulosa.image.format.Header
import nebulosa.image.format.HeaderCard
import nebulosa.io.SeekableSource
import okio.Sink

open class XisfHeader : AbstractHeader {

    constructor() : super()

    constructor(cards: Collection<HeaderCard>) : super(cards)

    override fun readOnly(): Header {
        TODO("Not yet implemented")
    }

    override fun clone(): Header {
        TODO("Not yet implemented")
    }

    override fun write(sink: Sink) {
        TODO("Not yet implemented")
    }

    override fun add(key: String, value: Boolean, comment: String) {
        TODO("Not yet implemented")
    }

    override fun add(key: String, value: Int, comment: String) {
        TODO("Not yet implemented")
    }

    override fun add(key: String, value: Double, comment: String) {
        TODO("Not yet implemented")
    }

    override fun add(key: String, value: String, comment: String) {
        TODO("Not yet implemented")
    }

    override fun read(source: SeekableSource) {
        TODO("Not yet implemented")
    }
}
