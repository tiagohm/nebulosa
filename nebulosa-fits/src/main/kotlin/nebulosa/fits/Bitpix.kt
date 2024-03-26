package nebulosa.fits

import nebulosa.image.format.HeaderCard
import nebulosa.image.format.ReadableHeader
import kotlin.math.abs

enum class Bitpix(val type: Class<out Number>, val code: Int) : HeaderCard by FitsHeaderCard.create(FitsKeyword.BITPIX, code) {
    BYTE(Byte::class.javaPrimitiveType!!, 8),
    SHORT(Short::class.javaPrimitiveType!!, 16),
    INTEGER(Int::class.javaPrimitiveType!!, 32),
    LONG(Long::class.javaPrimitiveType!!, 64),
    FLOAT(Float::class.javaPrimitiveType!!, -32),
    DOUBLE(Double::class.javaPrimitiveType!!, -64);

    @JvmField internal val byteLength = abs(code) / 8

    companion object {

        @JvmStatic
        fun from(header: ReadableHeader): Bitpix {
            return of(header.getInt(FitsKeyword.BITPIX, 0))
        }

        @JvmStatic
        fun of(code: Int) = when (code) {
            8 -> BYTE
            16 -> SHORT
            32 -> INTEGER
            64 -> LONG
            -32 -> FLOAT
            -64 -> DOUBLE
            else -> throw IllegalArgumentException("invalid BITPIX code: $code")
        }

        @JvmStatic
        fun of(type: Class<*>) = when (type) {
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
