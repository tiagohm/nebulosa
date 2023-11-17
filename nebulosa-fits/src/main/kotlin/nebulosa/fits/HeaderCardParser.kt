package nebulosa.fits

import nebulosa.log.loggerFor
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.math.min

internal class HeaderCardParser(private val line: CharSequence) {

    private var parsePos = 0

    var key = ""
        private set

    var value = ""
        private set

    var comment = ""
        private set

    var type: Class<*> = Nothing::class.java
        private set

    init {
        parseKey()
        parseValue()
        parseComment()
    }

    private fun parseKey() {
        // Find the '=' in the line, if any...
        val iEq: Int = line.indexOf('=')

        // The stem is in the first 8 characters or what precedes an '=' character
        // before that.
        var endStem = if (iEq >= 0 && iEq <= HeaderCard.MAX_KEYWORD_LENGTH) iEq else HeaderCard.MAX_KEYWORD_LENGTH
        endStem = min(line.length.toDouble(), endStem.toDouble()).toInt()
        val rawStem = line.substring(0, endStem).trim { it <= ' ' }

        // Check for space at the start of the keyword...
        if (endStem > 0 && rawStem.isNotEmpty()) {
            if (line[0].isWhitespace()) {
                LOG.warn("[$rawStem] Non-standard starting with a space (trimming).")
            }
        }

        val stem = rawStem.uppercase()

        if (stem != rawStem) {
            LOG.warn("[$rawStem] Non-standard lower-case letter(s) in base keyword.")
        }

        key = stem
        parsePos = endStem

        // If not using HIERARCH, then be very resilient,
        // and return whatever key the first 8 chars make...

        // If the line does not have an '=', can only be a simple key
        // If it's not a HIERARCH keyword, then return the simple key.
        if (iEq < 0 || stem != NonStandard.HIERARCH.key) {
            return
        }

        // Compose the hierarchical key...
        val tokens = StringTokenizer(line.substring(stem.length, iEq), " \t\r\n.")
        val builder = StringBuilder(stem)

        while (tokens.hasMoreTokens()) {
            val token = tokens.nextToken()
            parsePos = line.indexOf(token, parsePos) + token.length

            // Add a . to separate hierarchies
            builder.append('.')
            builder.append(token)
        }
        key = builder.toString()

        if (NonStandard.HIERARCH.key == key) {
            // The key is only HIERARCH, without a hierarchical keyword after it...
            LOG.warn("HIERARCH base keyword without HIERARCH-style long key after it.")
            return
        }

        // Case insensitive.
        key = key.uppercase()
    }

    private fun skipSpaces(): Boolean {
        while (parsePos < line.length) {
            if (!line[parsePos].isWhitespace()) {
                // Line has non-space characters left to parse...
                return true
            }

            parsePos++
        }

        return false
    }

    private fun parseComment() {
        if (!skipSpaces()) {
            // nothing left to parse.
            return
        }

        // if no value, then everything is comment from here on...
        if (value.isEmpty()) {
            if (line[parsePos] == '/') {
                // Skip the '/' itself, the comment is whatever is after it.
                parsePos++
            } else {
                // Junk after a string value -- interpret it as the start of the comment...
                LOG.warn("[$key] junk after value (included in the comment).")
            }
        }

        comment = line.substring(parsePos)
        parsePos = line.length
    }

    private fun parseValue() {
        if (key.isEmpty() || !skipSpaces()) {
            // nothing left to parse.
            return
        }

        if (Standard.CONTINUE.key == key) {
            parseValueBody()
        } else if (line[parsePos] == '=') {
            if (parsePos < HeaderCard.MAX_KEYWORD_LENGTH) {
                LOG.warn("[$key] assigmment before byte ${HeaderCard.MAX_KEYWORD_LENGTH + 1} for key '$key'.")
            }

            if (parsePos + 1 >= line.length) {
                LOG.warn("[$key] record ends with '='.")
            } else if (line[parsePos + 1] != ' ') {
                LOG.warn("[$key] missing required standard space after '='.")
            }

            if (parsePos > HeaderCard.MAX_KEYWORD_LENGTH) {
                // equal sign = after the 9th char -- only supported with hierarch keys...
                if (!key.startsWith(NonStandard.HIERARCH.key + ".")) {
                    LOG.warn("[$key] possibly misplaced '=' (after byte 9).")
                    // It's not a HIERARCH key
                    return
                }
            }

            parsePos++

            parseValueBody()
        }
    }

    private fun parseValueBody() {
        if (!skipSpaces()) {
            // nothing left to parse.
            return
        }

        if (isNextQuote()) {
            // Parse as a string value, or else throw an exception.
            parseStringValue()
        } else {
            var end = line.indexOf('/', parsePos)

            if (end < 0) {
                end = line.length
            }

            value = line.substring(parsePos, end).trim { it <= ' ' }
            parsePos = end
            type = getInferredValueType(key, value)
        }
    }

    private fun isNextQuote(): Boolean {
        return if (parsePos >= line.length) false
        else line[parsePos] == '\''
    }

    private fun getNoTrailingSpaceString(buf: StringBuilder): String {
        var to = buf.length

        // Remove trailing spaces only!
        while (--to >= 0) {
            if (!Character.isSpaceChar(buf[to])) {
                break
            }
        }

        return if (to < 0) ""
        else buf.substring(0, to + 1)
    }

    private fun parseStringValue() {
        type = String::class.java

        val buf = StringBuilder(HeaderCard.MAX_VALUE_LENGTH)

        // Build the string value, up to the end quote and paying attention to double
        // quotes inside the string, which are translated to single quotes within
        // the string value itself.
        ++parsePos

        while (parsePos < line.length) {
            if (isNextQuote()) {
                parsePos++

                if (!isNextQuote()) {
                    // Closing single quote;
                    value = getNoTrailingSpaceString(buf)
                    return
                }
            }

            buf.append(line[parsePos])
            parsePos++
        }

        LOG.warn("[$key] ignored missing end quote (value parsed to end of record).")
        value = getNoTrailingSpaceString(buf)
    }

    private fun getInferredValueType(key: String, value: String): Class<*> {
        if (value.isEmpty()) {
            LOG.warn("[$key] null non-string value (defaulted to Boolean.class).")
            return Boolean::class.javaPrimitiveType!!
        }

        val trimmedValue = value.trim().uppercase()

        if ("T" == trimmedValue || "F" == trimmedValue) {
            return Boolean::class.javaPrimitiveType!!
        }
        if (INT_REGEX.matches(trimmedValue)) {
            return getIntegerType(trimmedValue)
        }
        if (DECIMAL_REGEX.matches(trimmedValue)) {
            return getDecimalType(trimmedValue)
        }
        if (COMPLEX_REGEX.matches(trimmedValue)) {
            return ComplexValue::class.java
        }

        LOG.warn("[$key] unrecognised non-string value type '$trimmedValue'.")

        return Nothing::class.java
    }

    private fun getDecimalType(value: String): Class<out Number?> {
        var transformedValue = value.uppercase()
        val hasD = (transformedValue.indexOf('D') >= 0)

        if (hasD) {
            // Convert the Double Scientific Notation specified by FITS to pure IEEE.
            transformedValue = transformedValue.replace('D', 'E')
        }

        val big = BigDecimal(transformedValue)

        // Check for zero, and deal with it separately...
        if (big.stripTrailingZeros() == BigDecimal.ZERO) {
            val decimals = big.scale()

            if (decimals <= 7) {
                return if (hasD) Double::class.javaPrimitiveType!!
                else Float::class.javaPrimitiveType!!
            }

            return if (decimals <= 16) Double::class.javaPrimitiveType!!
            else BigDecimal::class.javaPrimitiveType!!
        }

        // Now non-zero values...
        val decimals = big.precision() - 1

        val f = big.toFloat()
        if ((decimals <= 7) && f != 0.0f && f.isFinite()) {
            return if (hasD) Double::class.javaPrimitiveType!!
            else Float::class.javaPrimitiveType!!
        }

        val d = big.toDouble()

        return if ((decimals <= 16) && d != 0.0 && d.isFinite()) Double::class.javaPrimitiveType!!
        else BigDecimal::class.java
    }

    private fun getIntegerType(value: String): Class<out Number> {
        val bits = BigInteger(value).bitLength()
        return if (bits < 32) Int::class.javaPrimitiveType!!
        else return if (bits < 64) Long::class.javaPrimitiveType!!
        else BigInteger::class.java
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<HeaderCardParser>()

        @JvmStatic private val DECIMAL_REGEX = Regex("[+-]?\\d+(\\.\\d*)?([dDeE][+-]?\\d+)?")
        @JvmStatic private val COMPLEX_REGEX = Regex("\\(\\s*$DECIMAL_REGEX\\s*,\\s*$DECIMAL_REGEX\\s*\\)")
        @JvmStatic private val INT_REGEX = Regex("[+-]?\\d+")

    }
}
