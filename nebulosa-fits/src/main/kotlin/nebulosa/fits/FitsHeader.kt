package nebulosa.fits

import nebulosa.image.format.AbstractHeader
import nebulosa.image.format.HeaderCard
import nebulosa.image.format.HeaderKey
import nebulosa.io.SeekableSource
import nebulosa.io.source
import nebulosa.log.d
import nebulosa.log.loggerFor
import java.util.*
import java.util.function.Predicate

open class FitsHeader : AbstractHeader {

    constructor() : super()

    constructor(cards: Collection<HeaderCard>) : super(cards)

    override fun readOnly(): FitsHeader = ReadOnly(this)

    override fun clone() = FitsHeader(this)

    override fun add(key: HeaderKey, value: Boolean) {
        checkType(key, ValueType.LOGICAL)
        FitsHeaderCard.create(key, value).also(::add)
    }

    override fun add(key: String, value: Boolean, comment: String) {
        FitsHeaderCard.create(key, value, comment).also(::add)
    }

    override fun add(key: HeaderKey, value: Int) {
        checkType(key, ValueType.INTEGER)
        FitsHeaderCard.create(key, value).also(::add)
    }

    override fun add(key: String, value: Int, comment: String) {
        FitsHeaderCard.create(key, value, comment).also(::add)
    }

    override fun add(key: HeaderKey, value: Double) {
        checkType(key, ValueType.REAL)
        FitsHeaderCard.create(key, value).also(::add)
    }

    override fun add(key: String, value: Double, comment: String) {
        FitsHeaderCard.create(key, value, comment).also(::add)
    }

    override fun add(key: HeaderKey, value: String) {
        checkType(key, ValueType.STRING)
        FitsHeaderCard.create(key, value).also(::add)
    }

    override fun add(key: String, value: String, comment: String) {
        FitsHeaderCard.create(key, value, comment).also(::add)
    }

    open class ReadOnly : FitsHeader {

        constructor() : super(LinkedList<HeaderCard>())

        constructor(cards: Collection<HeaderCard>) : super(cards)

        final override fun clear() = Unit

        final override fun add(key: HeaderKey, value: Boolean) = Unit

        final override fun add(key: HeaderKey, value: Int) = Unit

        final override fun add(key: HeaderKey, value: Double) = Unit

        final override fun add(key: HeaderKey, value: String) = Unit

        final override fun add(key: String, value: Boolean, comment: String) = Unit

        final override fun add(key: String, value: Int, comment: String) = Unit

        final override fun add(key: String, value: Double, comment: String) = Unit

        final override fun add(key: String, value: String, comment: String) = Unit

        final override fun add(element: HeaderCard) = false

        final override fun addAll(cards: Iterable<HeaderCard>) = Unit

        final override fun addAll(elements: Collection<HeaderCard>) = false

        final override fun removeIf(filter: Predicate<in HeaderCard>) = false

        final override fun remove(element: HeaderCard) = false

        final override fun removeAll(elements: Collection<HeaderCard>) = false

        final override fun retainAll(elements: Collection<HeaderCard>) = false

        final override fun delete(key: HeaderKey) = false

        final override fun delete(key: String) = false

        override fun readOnly() = this
    }

    companion object {

        const val DEFAULT_COMMENT_ALIGN = 30
        const val MIN_COMMENT_ALIGN = 20
        const val MAX_COMMENT_ALIGN = 70

        @JvmStatic val EMPTY = ReadOnly()
        private val LOG = loggerFor<FitsHeader>()

        var commentAlignPosition = DEFAULT_COMMENT_ALIGN
            set(value) {
                require(value in MIN_COMMENT_ALIGN..MAX_COMMENT_ALIGN)
                field = value
            }

        @JvmStatic
        fun from(source: SeekableSource): FitsHeader {
            return FitsFormat.readHeader(source)
        }

        @JvmStatic
        fun from(source: String): FitsHeader {
            return from(source.toByteArray())
        }

        @JvmStatic
        fun from(source: ByteArray): FitsHeader {
            return source.source().use(::from)
        }

        @JvmStatic
        fun checkType(key: HeaderKey, type: ValueType): Boolean {
            if (key is FitsHeaderKey) {
                if (key.valueType == type || key.valueType == ValueType.ANY) {
                    return true
                }

                if (key.valueType == ValueType.COMPLEX && (type == ValueType.REAL || type == ValueType.INTEGER)) {
                    return true
                }

                if (key.valueType == ValueType.REAL && type == ValueType.INTEGER) {
                    return true
                }

                LOG.d { debug("[{}] with unexpected value type. Expected {}, got {}", key.key, key.valueType, type) }

                return false
            }

            return true
        }

        @JvmStatic
        fun isFirstCard(key: String): Boolean {
            return FitsKeyword.SIMPLE.key == key || FitsKeyword.XTENSION.key == key
        }
    }
}
