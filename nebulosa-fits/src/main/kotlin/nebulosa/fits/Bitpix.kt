package nebulosa.fits

import kotlin.math.abs

enum class Bitpix(val type: Class<out Number>, val code: Int, val description: String) {
    BYTE(Byte::class.javaPrimitiveType!!, 8, "bytes"),
    SHORT(Short::class.javaPrimitiveType!!, 16, "16-bit integers"),
    INTEGER(Int::class.javaPrimitiveType!!, 32, "32-bit integers"),
    LONG(Long::class.javaPrimitiveType!!, 64, "64-bit integers"),
    FLOAT(Float::class.javaPrimitiveType!!, -32, "32-bit floating point"),
    DOUBLE(Double::class.javaPrimitiveType!!, -64, "64-bit floating point");

    val card = HeaderCard.create(Standard.BITPIX, code)

    val byteSize = abs(code) / 8

    companion object {

        @JvmStatic
        fun from(header: Header): Bitpix {
            return of(header.getInt(Standard.BITPIX, 0))
        }

        @JvmStatic
        fun of(code: Int): Bitpix {
            return when (code) {
                8 -> BYTE
                16 -> SHORT
                32 -> INTEGER
                64 -> LONG
                -32 -> FLOAT
                -64 -> DOUBLE
                else -> throw IllegalArgumentException("invalid BITPIX code: $code")
            }
        }

        @JvmStatic
        fun of(type: Class<*>): Bitpix {
            return when (type) {
                Byte::class.javaPrimitiveType,
                Byte::class.javaObjectType -> BYTE
                Short::class.javaPrimitiveType,
                Short::class.javaObjectType -> SHORT
                Int::class.javaPrimitiveType,
                Int::class.javaObjectType -> INTEGER
                Long::class.javaPrimitiveType,
                Long::class.javaObjectType -> LONG
                Float::class.javaPrimitiveType,
                Float::class.javaObjectType -> FLOAT
                Double::class.javaPrimitiveType,
                Double::class.javaObjectType -> DOUBLE
                else -> throw IllegalArgumentException("no BITPIX for type: $type")
            }
        }
    }
}
