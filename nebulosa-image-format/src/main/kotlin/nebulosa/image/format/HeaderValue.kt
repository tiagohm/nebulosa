package nebulosa.image.format

import org.apache.commons.numbers.complex.Complex
import java.math.BigDecimal
import java.math.BigInteger

interface HeaderValue {

    val value: String

    fun getBooleanValue(defaultValue: Boolean): Boolean {
        return if (value == "T") true else if (value == "F") false else defaultValue
    }

    fun <T> getNumericValue(asType: Class<out T>, defaultValue: T): T {
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
            defaultValue
        }
    }

    fun <T> getValue(asType: Class<out T>, defaultValue: T): T {
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

    fun formatted(): String
}
