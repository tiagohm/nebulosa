package nebulosa.fits

import java.util.*

data class ComplexValue(val real: Double, val imaginary: Double) {

    fun isZero() = real == 0.0 && imaginary == 0.0

    fun isFinite() = real.isFinite() && imaginary.isFinite()

    override fun toString() = "($real, $imaginary)"

    companion object {

        @JvmStatic val ZERO = ComplexValue(0.0, 0.0)
        @JvmStatic val ONE = ComplexValue(1.0, 0.0)
        @JvmStatic val I = ComplexValue(0.0, 1.0)

        @JvmStatic
        fun parse(text: String): ComplexValue {
            val trimmedText = text.trim().uppercase().replace('D', 'E')

            val hasOpeningBracket = trimmedText[0] == '('
            val hasClosingBracket = trimmedText[trimmedText.length - 1] == ')'

            var re = 0.0
            var im = 0.0

            if (!hasOpeningBracket && !hasClosingBracket) {
                // Use just the real value.
                re = trimmedText.toDouble()
                return ComplexValue(re, im)
            }

            val start = if (hasOpeningBracket) 1 else 0
            val end = if (hasClosingBracket) trimmedText.length - 1 else trimmedText.length

            val tokens = StringTokenizer(trimmedText.substring(start, end), ",; \t")

            if (tokens.hasMoreTokens()) {
                re = tokens.nextToken().toDouble()
            }
            if (tokens.hasMoreTokens()) {
                im = tokens.nextToken().toDouble()
            }

            return ComplexValue(re, im)
        }
    }
}
