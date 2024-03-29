package nebulosa.fits

import nebulosa.image.format.HeaderCardType
import org.apache.commons.numbers.complex.Complex
import java.math.BigDecimal
import java.math.BigInteger

enum class FitsHeaderCardType(@JvmField internal val type: Class<*>) : HeaderCardType {
    NONE(Nothing::class.javaObjectType),
    TEXT(String::class.javaObjectType),
    BOOLEAN(Boolean::class.javaObjectType),
    INTEGER(Long::class.javaObjectType),
    BIG_INTEGER(BigInteger::class.java),
    DECIMAL(Double::class.java),
    BIG_DECIMAL(BigDecimal::class.java),
    COMPLEX(Complex::class.java),
}
