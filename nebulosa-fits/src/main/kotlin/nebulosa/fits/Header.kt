package nebulosa.fits

import nebulosa.log.loggerFor
import java.io.EOFException
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.util.*

open class Header internal constructor(private val cards: LinkedList<HeaderCard>) : FitsElement, Collection<HeaderCard> by cards, Serializable {

    constructor() : this(LinkedList<HeaderCard>())

    constructor(cards: Collection<HeaderCard>) : this(LinkedList(cards))

    constructor(header: Header) : this(LinkedList(header.cards))

    fun clear() {
        cards.clear()
    }

    operator fun contains(key: String): Boolean {
        return cards.any { it.key == key }
    }

    operator fun contains(key: FitsHeader): Boolean {
        return key.key in this
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getBoolean(key: FitsHeader, defaultValue: Boolean = false): Boolean {
        return getBoolean(key.key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getInt(key: FitsHeader, defaultValue: Int): Int {
        return getInt(key.key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getLong(key: FitsHeader, defaultValue: Long): Long {
        return getLong(key.key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getFloat(key: FitsHeader, defaultValue: Float): Float {
        return getFloat(key.key, defaultValue)
    }

    fun getDouble(key: String, defaultValue: Double): Double {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getDouble(key: FitsHeader, defaultValue: Double): Double {
        return getDouble(key.key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String {
        val card = cards.firstOrNull { it.key == key } ?: return defaultValue
        return card.getValue(defaultValue)
    }

    fun getString(key: FitsHeader, defaultValue: String): String {
        return getString(key.key, defaultValue)
    }

    final override fun read(source: SeekableByteChannel) {
        clear()

        var count = 0
        val lineBuffer = ByteBuffer.allocate(80)

        while (true) {
            lineBuffer.clear()
            if (source.read(lineBuffer) != 80) throw EOFException()
            lineBuffer.flip()
            val card = HeaderCard.from(lineBuffer)

            count++

            if (cards.isEmpty()) {
                require(card.key == Standard.SIMPLE.key) { "[${card.key}] invalid keyword." }
            } else if (card.isBlank) {
                continue
            } else if (card.key == Standard.END.key) {
                break
            }

            cards.add(card)
        }

        val skipBytes = Hdu.computeRemainingBytesToSkip(count * 80L)
        if (skipBytes > 0L) source.position(source.position() + skipBytes)
    }

    fun add(key: FitsHeader, value: Boolean): HeaderCard {
        checkType(key, ValueType.LOGICAL)
        val card = HeaderCard.create(key, value)
        val index = cards.indexOfFirst { it.key == key.key }
        if (index >= 0) cards[index] = card
        else cards.add(card)
        return card
    }

    fun add(key: FitsHeader, value: Int): HeaderCard {
        checkType(key, ValueType.INTEGER)
        return HeaderCard.create(key, value).also(::add)
    }

    fun add(key: FitsHeader, value: Double): HeaderCard {
        checkType(key, ValueType.REAL)
        return HeaderCard.create(key, value).also(::add)
    }

    fun add(key: FitsHeader, value: String): HeaderCard {
        checkType(key, ValueType.STRING)
        return HeaderCard.create(key, value).also(::add)
    }

    fun add(card: HeaderCard) {
        if (!card.isKeyValuePair) cards.add(card)
        val index = cards.indexOfFirst { it.key == card.key }
        if (index >= 0) cards[index] = card
        else cards.add(card)
    }

    fun delete(key: FitsHeader) {
        cards.removeIf { it.key == key.key }
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

        @JvmStatic private val LOG = loggerFor<Header>()

        @JvmStatic
        fun from(source: SeekableByteChannel): Header {
            val header = Header()
            header.read(source)
            return header
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
    }
}
