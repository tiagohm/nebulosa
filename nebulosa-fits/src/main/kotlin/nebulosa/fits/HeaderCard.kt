package nebulosa.fits

import nebulosa.log.loggerFor
import org.apache.commons.numbers.complex.Complex
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer

data class HeaderCard(
    override val key: String, override val value: String,
    val comment: String, val type: Class<*>,
) : Serializable, Map.Entry<String, String> {

    internal constructor(parsed: HeaderCardParser) : this(
        parsed.key, parsed.value, parsed.comment, parsed.type
    )

    val isCommentStyle
        get() = type === Nothing::class.java

    val isKeyValuePair
        get() = !isCommentStyle && key.isNotEmpty()

    val isBooleanType
        get() = BOOLEAN_TYPES.any { it === type }

    val isStringType
        get() = type === String::class.javaObjectType

    val isDecimalType
        get() = DECIMAL_TYPES.any { it === type }
                || BigDecimal::class.java.isAssignableFrom(type)

    val isIntegerType
        get() = INTEGET_TYPES.any { it === type }

    val isNumericType
        get() = isDecimalType || isIntegerType

    val isBlank
        get() = if (!isCommentStyle || key.isNotBlank()) false else comment.isBlank()

    private fun getBooleanValue(defaultValue: Boolean): Boolean {
        return if (value == "T") true else if (value == "F") false else defaultValue
    }

    inline fun <reified T> getValue(defaultValue: T): T {
        return getValue(T::class.java, defaultValue)
    }

    fun <T> getValue(asType: Class<out T>, defaultValue: T): T {
        return if (isStringType) {
            asType.cast(value)
        } else if (value.isBlank()) {
            defaultValue
        } else if (isBooleanType) {
            asType.cast(getBooleanValue(defaultValue as Boolean))
        } else if (Complex::class.java.isAssignableFrom(asType)) {
            asType.cast(Complex.parse(value.trim().uppercase().replace('D', 'E')))
        } else if (isNumericType) {
            try {
                val decimal = BigDecimal(value.uppercase().replace('D', 'E'))

                if (Byte::class.javaObjectType.isAssignableFrom(asType)) {
                    asType.cast(decimal.toByte())
                } else if (Short::class.javaObjectType.isAssignableFrom(asType)) {
                    asType.cast(decimal.toShort())
                } else if (Int::class.javaObjectType.isAssignableFrom(asType)) {
                    asType.cast(decimal.toInt())
                } else if (Long::class.javaObjectType.isAssignableFrom(asType)) {
                    asType.cast(decimal.toLong())
                } else if (Float::class.javaObjectType.isAssignableFrom(asType)) {
                    asType.cast(decimal.toFloat())
                } else if (Double::class.javaObjectType.isAssignableFrom(asType)) {
                    asType.cast(decimal.toDouble())
                } else if (BigInteger::class.javaObjectType.isAssignableFrom(asType)) {
                    asType.cast(decimal.toBigInteger())
                } else if (String::class.javaObjectType.isAssignableFrom(asType)) {
                    asType.cast(decimal.toString())
                } else if (Boolean::class.javaObjectType.isAssignableFrom(asType)) {
                    asType.cast(decimal.toBigInteger().compareTo(BigInteger.ZERO) != 0)
                } else {
                    asType.cast(decimal)
                }
            } catch (e: NumberFormatException) {
                LOG.error("failed to parse numeric value", e)
                defaultValue
            }
        } else {
            throw IllegalArgumentException("unsupported class $asType")
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<HeaderCard>()

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

        @JvmStatic private val BOOLEAN_TYPES = arrayOf(
            Boolean::class.javaPrimitiveType!!, Boolean::class.javaObjectType,
        )

        @JvmStatic private val DECIMAL_TYPES = arrayOf(
            Float::class.javaPrimitiveType!!, Double::class.javaPrimitiveType!!,
            Float::class.javaObjectType, Double::class.javaObjectType,
        )

        @JvmStatic private val INTEGET_TYPES = arrayOf(
            Byte::class.javaPrimitiveType!!, Short::class.javaPrimitiveType!!,
            Int::class.javaPrimitiveType!!, Long::class.javaPrimitiveType!!,
            Byte::class.javaObjectType, Short::class.javaObjectType,
            Int::class.javaObjectType, Long::class.javaObjectType,
        )

        @JvmStatic
        fun from(source: ByteBuffer): HeaderCard {
            return from(Charsets.US_ASCII.decode(source))
        }

        @JvmStatic
        fun from(source: CharSequence): HeaderCard {
            return HeaderCard(HeaderCardParser(source))
        }

        @JvmStatic
        fun create(header: FitsHeader, value: Boolean): HeaderCard {
            return HeaderCard(header.key, if (value) "T" else "F", header.comment, Boolean::class.javaPrimitiveType!!)
        }

        @JvmStatic
        fun create(header: FitsHeader, value: Int): HeaderCard {
            return HeaderCard(header.key, "$value", header.comment, Int::class.javaPrimitiveType!!)
        }

        @JvmStatic
        fun create(header: FitsHeader, value: Long): HeaderCard {
            return HeaderCard(header.key, "$value", header.comment, Long::class.javaPrimitiveType!!)
        }

        @JvmStatic
        fun create(header: FitsHeader, value: Float): HeaderCard {
            return HeaderCard(header.key, "$value", header.comment, Float::class.javaPrimitiveType!!)
        }

        @JvmStatic
        fun create(header: FitsHeader, value: Double): HeaderCard {
            return HeaderCard(header.key, "$value", header.comment, Double::class.javaPrimitiveType!!)
        }

        @JvmStatic
        fun create(header: FitsHeader, value: String): HeaderCard {
            return HeaderCard(header.key, value, header.comment, String::class.javaObjectType)
        }
    }
}
