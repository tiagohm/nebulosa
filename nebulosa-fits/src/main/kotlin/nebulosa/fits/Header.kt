package nebulosa.fits

import nebulosa.io.SeekableSource
import nebulosa.io.source
import nebulosa.log.loggerFor
import okio.Buffer
import okio.Sink
import java.io.EOFException
import java.io.Serializable
import java.util.*

open class Header internal constructor(@JvmField internal val cards: LinkedList<HeaderCard>) :
    FitsElement, WritableHeader, ReadableHeader, Collection<HeaderCard> by cards, Serializable {

    constructor() : this(LinkedList<HeaderCard>())

    constructor(cards: Collection<HeaderCard>) : this(LinkedList(cards))

    constructor(header: Header) : this(LinkedList(header.cards))

    open fun readOnly(): Header = ReadOnlyHeader(this)

    override fun clear() {
        cards.clear()
    }

    override fun read(source: SeekableSource) {
        clear()

        var count = 0
        val buffer = Buffer()

        while (true) {
            buffer.clear()

            if (source.read(buffer, 80L) != 80L) throw EOFException()

            val card = HeaderCard.from(buffer)
            count++

            if (cards.isEmpty()) {
                require(isFirstCard(card.key)) { "Not a proper FITS header: ${card.key} at ${source.position - 80L} offset" }
            } else if (card.isBlank) {
                continue
            } else if (card.key == Standard.END.key) {
                break
            }

            add(card)
        }

        val skipBytes = Hdu.computeRemainingBytesToSkip(count * 80L)
        if (skipBytes > 0L) source.skip(skipBytes)

        buffer.clear()
    }

    override fun write(sink: Sink) {
        val buffer = Buffer()

        for (card in cards) {
            buffer.writeString(card.format().also { println(it) }, Charsets.US_ASCII)
        }

        if (cards.last.key != "END") {
            buffer.writeString(HeaderCard.END.format(), Charsets.US_ASCII)
        }

        var remainingBytes = Hdu.computeRemainingBytesToSkip(buffer.size)

        while (remainingBytes-- > 0) {
            buffer.writeByte(0)
        }

        buffer.readAll(sink)
    }

    override fun add(key: FitsHeader, value: Boolean): HeaderCard {
        checkType(key, ValueType.LOGICAL)
        val card = HeaderCard.create(key, value)
        val index = cards.indexOfFirst { it.key == key.key }
        if (index >= 0) cards[index] = card
        else cards.add(card)
        return card
    }

    override fun add(key: FitsHeader, value: Int): HeaderCard {
        checkType(key, ValueType.INTEGER)
        return HeaderCard.create(key, value).also(::add)
    }

    override fun add(key: FitsHeader, value: Double): HeaderCard {
        checkType(key, ValueType.REAL)
        return HeaderCard.create(key, value).also(::add)
    }

    override fun add(key: FitsHeader, value: String): HeaderCard {
        checkType(key, ValueType.STRING)
        return HeaderCard.create(key, value).also(::add)
    }

    override fun add(card: HeaderCard) {
        if (!card.isKeyValuePair) cards.add(card)
        else {
            val index = cards.indexOfFirst { it.key == card.key }
            if (index >= 0) cards[index] = card
            else cards.add(card)
        }
    }

    override fun delete(key: FitsHeader): Boolean {
        return cards.removeIf { it.key == key.key }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Header) return false

        if (cards != other.cards) return false

        return true
    }

    override fun hashCode(): Int {
        return cards.hashCode()
    }

    override fun toString(): String {
        return "Header(cards=$cards)"
    }

    companion object {

        @JvmStatic val EMPTY: Header = ReadOnlyHeader()

        const val DEFAULT_COMMENT_ALIGN = 30
        const val MIN_COMMENT_ALIGN = 20
        const val MAX_COMMENT_ALIGN = 70

        @JvmStatic private val LOG = loggerFor<Header>()

        var commentAlignPosition = DEFAULT_COMMENT_ALIGN
            set(value) {
                require(value in MIN_COMMENT_ALIGN..MAX_COMMENT_ALIGN)
                field = value
            }

        @JvmStatic
        fun from(source: SeekableSource): Header {
            val header = Header()
            header.read(source)
            return header
        }

        @JvmStatic
        fun from(source: String): Header {
            return from(source.toByteArray())
        }

        @JvmStatic
        fun from(source: ByteArray): Header {
            return from(source.source())
        }

        @JvmStatic
        fun checkType(key: FitsHeader, type: ValueType): Boolean {
            if (key.valueType == type || key.valueType == ValueType.ANY) {
                return true
            }
            if (key.valueType == ValueType.COMPLEX && (type == ValueType.REAL || type == ValueType.INTEGER)) {
                return true
            }
            if (key.valueType == ValueType.REAL && type == ValueType.INTEGER) {
                return true
            }

            LOG.warn("[${key.key}] with unexpected value type. Expected $type, got ${key.valueType}")

            return false
        }

        @JvmStatic
        fun isFirstCard(key: String): Boolean {
            return Standard.SIMPLE.key == key || Standard.XTENSION.key == key
        }
    }
}
