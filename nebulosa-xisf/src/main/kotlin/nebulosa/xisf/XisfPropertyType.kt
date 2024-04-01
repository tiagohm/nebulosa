package nebulosa.xisf

import nebulosa.image.format.HeaderCardType
import java.math.BigDecimal
import java.math.BigInteger

enum class XisfPropertyType(@JvmField val typeName: String) : HeaderCardType {
    BOOLEAN("Boolean"),
    INT8("Int8"),
    UINT8("UInt8"),
    INT16("Int16"),
    UINT16("UInt16"),
    INT32("Int32"),
    UINT32("UInt32"),
    INT64("Int64"),
    UINT64("UInt64"),
    INT128("Int128"),
    UINT128("UInt128"),
    FLOAT32("Float32"),
    FLOAT64("Float64"),
    FLOAT128("Float128"),
    COMPLEX32("Complex32"),
    COMPLEX64("Complex64"),
    COMPLEX128("Complex128"),
    STRING("String"),
    TIMEPOINT("TimePoint"),
    VECTOR_INT8("I8Vector"),
    VECTOR_UINT8("UI8Vector"),
    VECTOR_INT16("I16Vector"),
    VECTOR_UINT16("UI16Vector"),
    VECTOR_INT32("I32Vector"),
    VECTOR_UINT32("UI32Vector"),
    VECTOR_INT64("I64Vector"),
    VECTOR_UINT64("UI64Vector"),
    VECTOR_INT128("I128Vector"),
    VECTOR_UINT128("UI128Vector"),
    VECTOR_FLOAT32("F32Vector"),
    VECTOR_FLOAT64("F64Vector"),
    VECTOR_FLOAT128("F128Vector"),
    VECTOR_COMPLEX32("C32Vector"),
    VECTOR_COMPLEX64("C64Vector"),
    VECTOR_COMPLEX128("C128Vector"),
    MATRIX_INT8("I8Matrix"),
    MATRIX_UINT8("UI8Matrix"),
    MATRIX_INT16("I16Matrix"),
    MATRIX_UINT16("UI16Matrix"),
    MATRIX_INT32("I32Matrix"),
    MATRIX_UINT32("UI32Matrix"),
    MATRIX_INT64("I64Matrix"),
    MATRIX_UINT64("UI64Matrix"),
    MATRIX_INT128("I128Matrix"),
    MATRIX_UINT128("UI128Matrix"),
    MATRIX_FLOAT32("F32Matrix"),
    MATRIX_FLOAT64("F64Matrix"),
    MATRIX_FLOAT128("F128Matrix"),
    MATRIX_COMPLEX32("C32Matrix"),
    MATRIX_COMPLEX64("C64Matrix"),
    MATRIX_COMPLEX128("C128Matrix");

    companion object {

        @JvmStatic private val MAPPED = entries.associateBy { it.typeName }

        @JvmStatic
        fun fromTypeName(typeName: String) = MAPPED[typeName]

        @JvmStatic
        fun fromValue(value: String) = if (value.startsWith('\'') && value.endsWith('\'')) STRING
        else if (value == "T" || value == "F") BOOLEAN
        else if (value.isBlank()) STRING
        else if ('.' in value || 'D' in value || 'E' in value) getDecimalType(value)
        else getIntegerType(value)

        private fun getDecimalType(value: String): XisfPropertyType {
            val transformedValue = if ('D' in value) {
                // Convert the Double Scientific Notation specified by FITS to pure IEEE.
                value.uppercase().replace('D', 'E')
            } else {
                value
            }

            val big = BigDecimal(transformedValue)

            // Check for zero, and deal with it separately...
            if (big.stripTrailingZeros().compareTo(BigDecimal.ZERO) == 0) {
                val decimals = big.scale()

                return if (decimals <= 7) FLOAT32
                else if (decimals <= 16) FLOAT64
                else FLOAT128
            }

            // Now non-zero values...
            val decimals = big.precision() - 1

            val f = big.toFloat()
            if (decimals <= 7 && f != 0.0f && f.isFinite()) {
                return FLOAT32
            }

            val d = big.toDouble()

            return if (decimals <= 16 && d != 0.0 && d.isFinite()) FLOAT64
            else FLOAT128
        }

        private fun getIntegerType(value: String): XisfPropertyType {
            val bits = BigInteger(value).bitLength()
            return if (bits < 32) INT32
            else if (bits < 64) INT64
            else INT128
        }
    }
}
