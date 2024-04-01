package nebulosa.xisf

import nebulosa.fits.FitsHeaderCardFormatter
import nebulosa.image.format.HeaderCard
import nebulosa.image.format.HeaderKey
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger

data class XisfHeaderCard(
    override val key: String, override val value: String,
    override val comment: String, override val type: XisfPropertyType,
) : HeaderCard, Serializable {

    override val isCommentStyle
        get() = false

    override val isKeyValuePair
        get() = !isCommentStyle && key.isNotEmpty()

    override val isBooleanType
        get() = type == XisfPropertyType.BOOLEAN

    override val isStringType
        get() = type == XisfPropertyType.STRING ||
                type == XisfPropertyType.TIMEPOINT

    override val isDecimalType
        get() = type == XisfPropertyType.FLOAT32 ||
                type == XisfPropertyType.FLOAT64 ||
                type == XisfPropertyType.FLOAT128

    override val isIntegerType
        get() = type == XisfPropertyType.UINT8 ||
                type == XisfPropertyType.UINT16 ||
                type == XisfPropertyType.UINT32 ||
                type == XisfPropertyType.UINT64 ||
                type == XisfPropertyType.UINT128 ||
                type == XisfPropertyType.INT8 ||
                type == XisfPropertyType.INT16 ||
                type == XisfPropertyType.INT32 ||
                type == XisfPropertyType.INT64 ||
                type == XisfPropertyType.INT128

    override fun formatted(): String {
        return FitsHeaderCardFormatter.format(this)
    }

    companion object {

        @JvmStatic
        fun create(header: HeaderKey, value: Boolean): XisfHeaderCard {
            return create(header.key, value, header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: Int): XisfHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: Long): XisfHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: BigInteger): XisfHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: Float): XisfHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: Double): XisfHeaderCard {
            return create(header.key, "$value", header.comment)
        }

        @JvmStatic
        fun create(key: HeaderKey, value: BigDecimal, comment: String = ""): XisfHeaderCard {
            return create(key.key, "$value", comment)
        }

        @JvmStatic
        fun create(header: HeaderKey, value: String): XisfHeaderCard {
            return create(header.key, value, header.comment)
        }

        @JvmStatic
        fun create(key: String, value: Boolean, comment: String = ""): XisfHeaderCard {
            return XisfHeaderCard(key, if (value) "T" else "F", comment, XisfPropertyType.BOOLEAN)
        }

        @JvmStatic
        fun create(key: String, value: Int, comment: String = ""): XisfHeaderCard {
            return XisfHeaderCard(key, "$value", comment, XisfPropertyType.INT32)
        }

        @JvmStatic
        fun create(key: String, value: Long, comment: String = ""): XisfHeaderCard {
            return XisfHeaderCard(key, "$value", comment, XisfPropertyType.INT64)
        }

        @JvmStatic
        fun create(key: String, value: BigInteger, comment: String = ""): XisfHeaderCard {
            return XisfHeaderCard(key, "$value", comment, XisfPropertyType.INT128)
        }

        @JvmStatic
        fun create(key: String, value: Float, comment: String = ""): XisfHeaderCard {
            return XisfHeaderCard(key, "$value", comment, XisfPropertyType.FLOAT32)
        }

        @JvmStatic
        fun create(key: String, value: Double, comment: String = ""): XisfHeaderCard {
            return XisfHeaderCard(key, "$value", comment, XisfPropertyType.FLOAT64)
        }

        @JvmStatic
        fun create(key: String, value: BigDecimal, comment: String = ""): XisfHeaderCard {
            return XisfHeaderCard(key, "$value", comment, XisfPropertyType.FLOAT128)
        }

        @JvmStatic
        fun create(key: String, value: String, comment: String = ""): XisfHeaderCard {
            return XisfHeaderCard(key, value, comment, XisfPropertyType.STRING)
        }
    }
}
