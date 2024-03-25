package nebulosa.fits

import nebulosa.image.format.HeaderCard
import nebulosa.image.format.HeaderKey
import nebulosa.log.loggerFor
import okio.Buffer
import org.apache.commons.numbers.complex.Complex
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.max

data class FitsHeaderCard(
    override val key: String, override val value: String,
    override val comment: String, val type: FitsHeaderCardType,
) : HeaderCard, Serializable {

    internal constructor(parsed: FitsHeaderCardParser) : this(
        parsed.key, parsed.value, parsed.comment, parsed.type
    )

    override val isCommentStyle
        get() = type == FitsHeaderCardType.NONE

    override val isKeyValuePair
        get() = !isCommentStyle && key.isNotEmpty()

    override val isBooleanType
        get() = type == FitsHeaderCardType.BOOLEAN

    override val isStringType
        get() = type == FitsHeaderCardType.TEXT

    override val isDecimalType
        get() = type == FitsHeaderCardType.DECIMAL || type == FitsHeaderCardType.BIG_DECIMAL

    override val isIntegerType
        get() = type == FitsHeaderCardType.INTEGER || type == FitsHeaderCardType.BIG_INTEGER

    override val isBlank
        get() = if (!isCommentStyle || key.isNotBlank()) false else comment.isBlank()

    val hasHierarchKey
        get() = isHierarchKey(key)

    private fun getBooleanValue(defaultValue: Boolean): Boolean {
        return if (value == "T") true else if (value == "F") false else defaultValue
    }

    private fun <T> getNumericValue(asType: Class<out T>, defaultValue: T): T {
        return try {
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
            } else if (BigDecimal::class.javaObjectType.isAssignableFrom(asType)) {
                asType.cast(decimal)
            } else {
                throw IllegalArgumentException("unsupported class $asType")
            }
        } catch (e: NumberFormatException) {
            LOG.error("failed to parse numeric value", e)
            defaultValue
        }
    }

    override fun <T> getValue(asType: Class<out T>, defaultValue: T): T {
        return if (Boolean::class.javaObjectType.isAssignableFrom(asType)) {
            asType.cast(getBooleanValue(defaultValue as Boolean))
        } else if (Number::class.javaObjectType.isAssignableFrom(asType)) {
            getNumericValue(asType, defaultValue)
        } else if (String::class.javaObjectType.isAssignableFrom(asType)) {
            asType.cast(value)
        } else if (Complex::class.java.isAssignableFrom(asType)) {
            asType.cast(Complex.parse(value.trim().uppercase().replace('D', 'E')))
        } else if (value.isBlank()) {
            defaultValue
        } else {
            throw IllegalArgumentException("unsupported class $asType")
        }
    }

    override fun formattedValue(): String {
        return FitsHeaderCardFormatter.format(this)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<FitsHeaderCard>()

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
        const val HIERARCH_WITH_DOT = "HIERARCH."

        @JvmStatic val END = FitsHeaderCard("END", "", "", FitsHeaderCardType.NONE)

        @JvmStatic
        fun from(source: Buffer): FitsHeaderCard {
            return from(source.readString(80L, Charsets.US_ASCII))
        }

        @JvmStatic
        fun from(source: CharSequence): FitsHeaderCard {
            return FitsHeaderCard(FitsHeaderCardParser(source))
        }

        @JvmStatic
        fun create(header: HeaderKey, value: Boolean): FitsHeaderCard {
            return create(header.key, value, header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: Int): FitsHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: Long): FitsHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: BigInteger): FitsHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: Float): FitsHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: Double): FitsHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(key: HeaderKey, value: BigDecimal, comment: String = ""): FitsHeaderCard {
            return create(key.key, "$value", comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: String): FitsHeaderCard {
            return create(header.key, value, header.comment)
        }

        @JvmStatic
        fun create(key: String, value: Boolean, comment: String = ""): FitsHeaderCard {
            return FitsHeaderCard(key, if (value) "T" else "F", comment, FitsHeaderCardType.BOOLEAN)
        }

        @JvmStatic
        fun create(key: String, value: Int, comment: String = ""): FitsHeaderCard {
            return FitsHeaderCard(key, "$value", comment, FitsHeaderCardType.INTEGER)
        }

        @JvmStatic
        fun create(key: String, value: Long, comment: String = ""): FitsHeaderCard {
            return FitsHeaderCard(key, "$value", comment, FitsHeaderCardType.INTEGER)
        }

        @JvmStatic
        fun create(key: String, value: BigInteger, comment: String = ""): FitsHeaderCard {
            return FitsHeaderCard(key, "$value", comment, FitsHeaderCardType.BIG_INTEGER)
        }

        @JvmStatic
        fun create(key: String, value: Float, comment: String = ""): FitsHeaderCard {
            return FitsHeaderCard(key, "$value", comment, FitsHeaderCardType.DECIMAL)
        }

        @JvmStatic
        fun create(key: String, value: Double, comment: String = ""): FitsHeaderCard {
            return FitsHeaderCard(key, "$value", comment, FitsHeaderCardType.DECIMAL)
        }

        @JvmStatic
        fun create(key: String, value: BigDecimal, comment: String = ""): FitsHeaderCard {
            return FitsHeaderCard(key, "$value", comment, FitsHeaderCardType.BIG_DECIMAL)
        }

        @JvmStatic
        fun create(key: String, value: String, comment: String = ""): FitsHeaderCard {
            return FitsHeaderCard(key, value, comment, FitsHeaderCardType.TEXT)
        }

        @JvmStatic
        internal fun isHierarchKey(key: String): Boolean {
            return key.uppercase().startsWith(HIERARCH_WITH_DOT)
        }

        @JvmStatic
        fun isValidChar(c: Char): Boolean {
            return c in MIN_VALID_CHAR..MAX_VALID_CHAR
        }

        @JvmStatic
        fun sanitize(input: CharSequence): String {
            val data = CharArray(input.length)

            for (i in input.indices) {
                val char = input[i]
                data[i] = if (isValidChar(char)) char else '?'
            }

            return data.concatToString()
        }

        @JvmStatic
        private fun spaceForValue(key: String): Int {
            return if (key.length > MAX_KEYWORD_LENGTH) {
                // HierarchFormater.extraSpaceRequired = 1
                FITS_HEADER_CARD_SIZE - (max(key.length, MAX_KEYWORD_LENGTH) + 1)
            } else {
                // DEFAULT_SKIP_BLANK_AFTER_ASSIGN = false, so AssignLength = 2.
                FITS_HEADER_CARD_SIZE - (max(key.length, MAX_KEYWORD_LENGTH) + 2)
            }
        }
    }
}
