package nebulosa.fits

import nom.tam.util.ComplexValue
import okio.BufferedSource
import java.math.BigDecimal
import java.math.BigInteger

class HeaderCard {

    var key = ""
        private set

    var value = ""
        private set

    var comment = ""
        private set

    var type: Class<*> = Nothing::class.java
        private set

    internal constructor(parsed: HeaderCardParser) {
        key = parsed.key
        value = parsed.value
        comment = parsed.comment
        type = parsed.type
    }

    constructor(source: BufferedSource) : this(HeaderCardParser(source.readString(80, Charsets.US_ASCII)))

    val isCommentStyleCard
        get() = type === Nothing::class.java

    val isKeyValuePair
        get() = !isCommentStyleCard && key.isNotEmpty()

    val isBooleanType
        get() = Boolean::class.java.isAssignableFrom(type)

    val isStringType
        get() = String::class.java.isAssignableFrom(type)

    val isDecimalType
        get() = Float::class.java.isAssignableFrom(type)
                || Double::class.java.isAssignableFrom(type)
                || BigDecimal::class.java.isAssignableFrom(type)

    val isIntegerType
        get() = isNumericType && !isDecimalType

    val isNumericType
        get() = Number::class.java.isAssignableFrom(type)

    val isBlank
        get() = if (!isCommentStyleCard || key.isNotEmpty()) false else comment.isEmpty()

    private fun getBooleanValue(defaultValue: Boolean): Boolean {
        return if ("T" == value) true else if ("F" == value) false else defaultValue
    }

    inline fun <reified T> getValue(defaultValue: T): T {
        return getValue(T::class.java, defaultValue)
    }

    fun <T> getValue(asType: Class<out T>, defaultValue: T): T {
        return if (isStringType) {
            asType.cast(value)
        } else if (value.isEmpty()) {
            defaultValue
        } else if (isBooleanType) {
            asType.cast(getBooleanValue(defaultValue as Boolean))
        } else if (ComplexValue::class.java.isAssignableFrom(asType)) {
            asType.cast(ComplexValue(value))
        } else if (isNumericType) {
            try {
                val decimal = BigDecimal(value.uppercase().replace('D', 'E'))

                if (Byte::class.java.isAssignableFrom(asType)) {
                    asType.cast(decimal.toByte())
                } else if (Short::class.java.isAssignableFrom(asType)) {
                    asType.cast(decimal.toShort())
                } else if (Int::class.java.isAssignableFrom(asType)) {
                    asType.cast(decimal.toInt())
                } else if (Long::class.java.isAssignableFrom(asType)) {
                    asType.cast(decimal.toLong())
                } else if (Float::class.java.isAssignableFrom(asType)) {
                    asType.cast(decimal.toFloat())
                } else if (Double::class.java.isAssignableFrom(asType)) {
                    asType.cast(decimal.toDouble())
                } else if (BigInteger::class.java.isAssignableFrom(asType)) {
                    asType.cast(decimal.toBigInteger())
                } else {
                    asType.cast(decimal)
                }
            } catch (e: NumberFormatException) {
                defaultValue
            }
        } else {
            throw IllegalArgumentException("unsupported class $asType")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeaderCard

        if (key != other.key) return false
        if (value != other.value) return false
        if (comment != other.comment) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + comment.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString() = "HeaderCard(key='$key', value='$value', comment='$comment', type=$type)"

    companion object {

        const val FITS_HEADER_CARD_SIZE = 80
        const val MAX_KEYWORD_LENGTH = 8
        const val STRING_QUOTES_LENGTH = 2
        const val MAX_VALUE_LENGTH = 70
        const val MAX_COMMENT_CARD_COMMENT_LENGTH = MAX_VALUE_LENGTH + 1
        const val MAX_STRING_VALUE_LENGTH = MAX_VALUE_LENGTH - 2
        const val MAX_LONG_STRING_VALUE_LENGTH = MAX_STRING_VALUE_LENGTH - 1
        const val MAX_LONG_STRING_VALUE_WITH_COMMENT_LENGTH = MAX_LONG_STRING_VALUE_LENGTH - 2
        const val MAX_HIERARCH_KEYWORD_LENGTH = FITS_HEADER_CARD_SIZE - 6
        const val MAX_LONG_STRING_CONTINUE_OVERHEAD = 3
        const val MIN_VALID_CHAR = 0x20.toChar()
        const val MAX_VALID_CHAR = 0x7e.toChar()
        const val EMPTY_KEY = ""
    }
}
