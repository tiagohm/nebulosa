package nebulosa.fits

import nebulosa.log.d
import nebulosa.log.loggerFor
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.math.min

internal data class FitsHeaderCardParser(private val line: CharSequence) {

    private var parsePos = 0

    @JvmField internal var key = ""
    @JvmField internal var value = ""
    @JvmField internal var comment = ""
    @JvmField internal var type = FitsHeaderCardType.NONE

    init {
        parseKey()
        parseValue()
        parseComment()
    }

    private fun parseKey() {
        // Find the '=' in the line, if any...
        val iEq = line.indexOf('=')

        // The stem is in the first 8 characters or what precedes an '=' character
        // before that.
        var endStem = if (iEq in 0..FitsHeaderCard.MAX_KEYWORD_LENGTH) iEq else FitsHeaderCard.MAX_KEYWORD_LENGTH
        endStem = min(line.length, endStem)
        val rawStem = line.substring(0, endStem).trim { it <= ' ' }

        // Check for space at the start of the keyword...
        if (endStem > 0 && rawStem.isNotEmpty()) {
            if (line[0].isWhitespace()) {
                LOG.d("[{}] Non-standard starting with a space (trimming)", rawStem)
            }
        }

        val stem = rawStem.uppercase()

        if (stem != rawStem) {
            LOG.d("[{}] Non-standard lower-case letter(s) in base keyword", rawStem)
        }

        key = stem
        parsePos = endStem

        // If not using HIERARCH, then be very resilient,
        // and return whatever key the first 8 chars make...

        // If the line does not have an '=', can only be a simple key
        // If it's not a HIERARCH keyword, then return the simple key.
        if (iEq < 0 || stem != FitsKeyword.HIERARCH.key) {
            return
        }

        // Compose the hierarchical key...
        val tokens = StringTokenizer(line.substring(stem.length, iEq), " \t\r\n")
        val builder = StringBuilder(stem)

        while (tokens.hasMoreTokens()) {
            val token = tokens.nextToken()
            parsePos = line.indexOf(token, parsePos) + token.length

            // Add a . to separate hierarchies
            builder.append('.')
            builder.append(token)
        }

        key = builder.toString()

        if (FitsKeyword.HIERARCH.key == key) {
            // The key is only HIERARCH, without a hierarchical keyword after it...
            LOG.d("HIERARCH base keyword without HIERARCH-style long key after it")
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
        if (value.isNotEmpty()) {
            if (line[parsePos] == '/') {
                // Skip the '/' itself, the comment is whatever is after it.
                parsePos++
            } else {
                // Junk after a string value -- interpret it as the start of the comment...
                LOG.d("[{}] junk after value (included in the comment)", key)
            }
        }

        comment = line.substring(parsePos).trim()
        parsePos = line.length
    }

    private fun parseValue() {
        if (key.isEmpty() || !skipSpaces()) {
            // nothing left to parse.
            return
        }

        if (FitsKeyword.CONTINUE.key == key) {
            parseValueBody()
        } else if (line[parsePos] == '=') {
            if (parsePos < FitsHeaderCard.MAX_KEYWORD_LENGTH) {
                LOG.d("[{}] assigmment before byte {}", key, FitsHeaderCard.MAX_KEYWORD_LENGTH + 1)
            }

            if (parsePos + 1 >= line.length) {
                LOG.d("[{}] record ends with '='", key)
            } else if (line[parsePos + 1] != ' ') {
                LOG.d("[{}] missing required standard space after '='", key)
            }

            if (parsePos > FitsHeaderCard.MAX_KEYWORD_LENGTH) {
                // equal sign = after the 9th char -- only supported with hierarch keys...
                if (!key.startsWith(FitsKeyword.HIERARCH.key + "")) {
                    LOG.d("[{}] possibly misplaced '=' (after byte 9)", key)
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
        type = FitsHeaderCardType.TEXT

        val buf = StringBuilder(FitsHeaderCard.MAX_VALUE_LENGTH)

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

        LOG.d("[{}] ignored missing end quote (value parsed to end of record)", key)
        value = getNoTrailingSpaceString(buf)
    }

    private fun getInferredValueType(key: String, value: String): FitsHeaderCardType {
        if (value.isEmpty()) {
            LOG.d("[{}] null non-string value (defaulted to Boolean)", key)
            return FitsHeaderCardType.BOOLEAN
        }

        val trimmedValue = value.trim().uppercase()

        if ("T" == trimmedValue || "F" == trimmedValue) {
            return FitsHeaderCardType.BOOLEAN
        }
        if (INT_REGEX.matches(trimmedValue)) {
            return getIntegerType(trimmedValue)
        }
        if (DECIMAL_REGEX.matches(trimmedValue)) {
            return getDecimalType(trimmedValue)
        }
        if (COMPLEX_REGEX.matches(trimmedValue)) {
            return FitsHeaderCardType.COMPLEX
        }

        LOG.d("[{}] unrecognised non-string value type '{}'", key, trimmedValue)

        return FitsHeaderCardType.NONE
    }

    private fun getDecimalType(value: String): FitsHeaderCardType {
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

            return if (decimals <= 16) FitsHeaderCardType.DECIMAL
            else FitsHeaderCardType.BIG_DECIMAL
        }

        // Now non-zero values...
        val decimals = big.precision() - 1

        val f = big.toFloat()
        if (decimals <= 7 && f != 0.0f && f.isFinite()) {
            return FitsHeaderCardType.DECIMAL
        }

        val d = big.toDouble()

        return if (decimals <= 16 && d != 0.0 && d.isFinite()) FitsHeaderCardType.DECIMAL
        else FitsHeaderCardType.BIG_DECIMAL
    }

    private fun getIntegerType(value: String): FitsHeaderCardType {
        val bits = BigInteger(value).bitLength()
        return if (bits < 64) FitsHeaderCardType.INTEGER
        else FitsHeaderCardType.BIG_INTEGER
    }

    companion object {

        const val MIN_VALID_CHAR = 0x20.toChar()
        const val MAX_VALID_CHAR = 0x7e.toChar()

        @JvmStatic private val LOG = loggerFor<FitsHeaderCardParser>()

        @JvmStatic private val DECIMAL_REGEX = Regex("[+-]?\\d+(\\.\\d*)?([dDeE][+-]?\\d+)?")
        @JvmStatic private val COMPLEX_REGEX = Regex("\\(\\s*$DECIMAL_REGEX\\s*,\\s*$DECIMAL_REGEX\\s*\\)")
        @JvmStatic private val INT_REGEX = Regex("[+-]?\\d+")

        @JvmStatic
        fun isValidChar(c: Char): Boolean {
            return c in MIN_VALID_CHAR..MAX_VALID_CHAR
        }
    }
}
