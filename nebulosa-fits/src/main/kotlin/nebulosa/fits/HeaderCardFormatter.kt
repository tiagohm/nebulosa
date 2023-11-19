package nebulosa.fits

import java.util.*
import kotlin.math.min

object HeaderCardFormatter {

    fun format(card: HeaderCard): String {
        return with(StringBuilder(HeaderCard.FITS_HEADER_CARD_SIZE)) {
            appendKey(card)

            val valueStart = appendValue(card)
            val valueEnd = length

            appendComment(card)

            if (!card.isCommentStyle) {
                // Strings must be left aligned with opening quote in byte 11 (counted from 1).
                realign(if (card.isStringType) valueEnd else valueStart, valueEnd)
            }

            pad()

            HeaderCard.sanitize(this)
        }
    }

    @JvmStatic
    private fun StringBuilder.appendKey(card: HeaderCard) {
        var key = card.key

        if (card.hasHierarchKey) {
            key = HierarchKeyFormatter.INSTANCE.format(key)

            if (key.length > HeaderCard.MAX_HIERARCH_KEYWORD_LENGTH) {
                // Truncate HIERARCH keywords as necessary to fit.
                // This is really just a second parachute here. Normally, HeaderCards
                // won't allow creation or setting longer keywords...
                // throw LongValueException(key, HeaderCard.MAX_HIERARCH_KEYWORD_LENGTH)
            }
        } else {
            // Just to be certain, we'll make sure base keywords are upper-case, if they
            // were not already.
            key = key.uppercase(Locale.getDefault())
        }

        append(key)

        padTo(HeaderCard.MAX_KEYWORD_LENGTH)
    }

    @JvmStatic
    private fun StringBuilder.appendValue(card: HeaderCard): Int {
        val value = card.value

        if (card.isCommentStyle) {
            // Comment-style card. Nothing to do here...
            return length
        }

        // Add assignment sequence "= "
        append("= ")

        if (value.isEmpty()) {
            // 'null' value, nothing more to append.
            return length
        }

        val valueStart = length

        if (card.isStringType) {
            var from = appendQuotedValue(card, 0)

            while (from < value.length) {
                pad()
                append(Standard.CONTINUE.key + "  ")
                from += appendQuotedValue(card, from)
            }
        } else {
            append(value, 0)
        }

        return valueStart
    }

    @JvmStatic
    private fun StringBuilder.appendQuotedValue(card: HeaderCard, from: Int): Int {
        // Always leave room for an extra & character at the end...
        var available = availableCharCount() - QUOTES_LENGTH

        // If long strings are enabled leave space for '&' at the end.
        if (card.comment.isNotEmpty()) {
            available--
        }

        val text = card.value

        // The remaining part of the string fits in the space with the
        // quoted quotes, then it's easy...
        if (available >= text.length - from) {
            val escaped = text.substring(from).replace("'", "''")

            if (escaped.length <= available) {
                append('\'')
                append(escaped)

                // Earlier versions of the FITS standard required that the closing quote
                // does not come before byte 20. It's no longer required but older tools
                // may still expect it, so let's conform. This only affects single
                // record card, but not continued long strings...
                if (length < MIN_STRING_END) {
                    padTo(MIN_STRING_END)
                }

                append('\'')

                return text.length - from
            }
        }

        // Now, we definitely need space for '&' at the end...
        available = availableCharCount() - QUOTES_LENGTH - 1

        // Opening quote
        append("'")

        // For counting the characters consumed from the input
        var consumed = 0
        var i = 0

        while (i < available) {
            val c = text[from + consumed]

            if (c == '\'') {
                // Quoted quotes take up 2 spaces...
                i++

                if (i + 1 >= available) {
                    // Otherwise leave the value quote unconsumed.
                    break
                }

                // Only append the quoted quote if there is room for both.
                append("''")
            } else {
                // Append a non-quote character.
                append(c)
            }

            i++
            consumed++
        }

        // & and Closing quote.
        append("&'")

        return consumed
    }

    @JvmStatic
    private fun StringBuilder.appendLongStringComment(card: HeaderCard) {
        // We can wrap the comment to our delight, with CONTINUE!
        val iLast = length - 1
        val comment = card.comment

        // We need to amend the last string to end with '&'
        if (availableCharCount() >= LONG_COMMENT_PREFIX.length + comment.length) {
            // We can append the entire comment, easy...
            append(LONG_COMMENT_PREFIX)
            append(comment, 0)
            return
        }

        // Add '&' to the end of the string value.
        // appendQuotedValue() must always leave space for it!
        setCharAt(iLast, '&')
        append("'")

        val available = availableCharCount()

        // If there is room for a standard inline comment, then go for it
        if (available < COMMENT_PREFIX.length) {
            // Add a CONTINUE card with an empty string and try again...
            pad()
            append(Standard.CONTINUE.key + "  ''")
            appendComment(card)
            return
        }

        append(COMMENT_PREFIX)
        var from = append(comment, 0)

        // Now add records as needed to write the comment fully...
        while (from < comment.length) {
            pad()
            append(Standard.CONTINUE.key + "  ")
            append(if (comment.length >= from + MAX_LONG_END_COMMENT) "'&'" else "''")
            append(LONG_COMMENT_PREFIX)
            from += append(comment, from)
        }
    }

    @JvmStatic
    private fun StringBuilder.realign(at: Int, from: Int): Boolean {
        return if (length >= HeaderCard.FITS_HEADER_CARD_SIZE || from >= Header.commentAlignPosition) {
            // We are beyond the alignment point already...
            false
        } else {
            realign(at, from, Header.commentAlignPosition)
        }
    }

    @JvmStatic
    private fun StringBuilder.appendComment(card: HeaderCard): Boolean {
        val comment = card.comment

        if (comment.isEmpty()) {
            return true
        }

        var available = availableCharCount()
        val longCommentOK = card.isStringType

        if (!card.isCommentStyle && longCommentOK) {
            if (COMMENT_PREFIX.length + card.comment.length > available) {
                // No room for a complete regular comment, but we can do a long string comment...
                appendLongStringComment(card)
                return true
            }
        }

        if (card.isCommentStyle) {
            // ' ' instead of '= '
            available--
        } else {
            // ' / '
            available -= COMMENT_PREFIX.length

            if (card.minTruncatedCommentSize() > available) {
                if (!longCommentOK) {
                    return false
                }
            }
        }

        if (card.isCommentStyle) {
            append(' ')
        } else {
            append(COMMENT_PREFIX)
        }

        if (available >= comment.length) {
            append(comment)
            return true
        }

        append(comment.substring(0, available))

        return false
    }

    private const val QUOTES_LENGTH = 2
    private const val COMMENT_PREFIX = " / "
    private const val LONG_COMMENT_PREFIX = " /"
    private const val MIN_STRING_END = 19
    private const val MAX_LONG_END_COMMENT = 68 - LONG_COMMENT_PREFIX.length

    @JvmStatic
    private fun StringBuilder.realign(at: Int, from: Int, to: Int): Boolean {
        val spaces = to - from
        if (spaces > availableCharCount()) {
            // No space left in card to align the specified position.
            return false
        }

        val buffer = StringBuffer(spaces)

        repeat(spaces) {
            buffer.append(' ')
        }

        insert(at, buffer)

        return true
    }

    @JvmStatic
    private fun StringBuilder.append(text: String, from: Int): Int {
        val available = availableCharCount()
        val n = min(available.toDouble(), (text.length - from).toDouble()).toInt()

        if (n < 1) {
            return 0
        }

        repeat(n) {
            append(text[from + it])
        }

        return n
    }

    @JvmStatic
    private fun StringBuilder.pad(n: Int = availableCharCount()) {
        repeat(n) { append(' ') }
    }

    @JvmStatic
    private fun StringBuilder.padTo(to: Int) {
        for (pos in length % HeaderCard.FITS_HEADER_CARD_SIZE until to) {
            append(' ')
        }
    }

    @JvmStatic
    private fun StringBuilder.availableCharCount(): Int {
        return (HeaderCard.FITS_HEADER_CARD_SIZE - length % HeaderCard.FITS_HEADER_CARD_SIZE) % HeaderCard.FITS_HEADER_CARD_SIZE
    }

    @JvmStatic
    private fun HeaderCard.minTruncatedCommentSize(): Int {
        var firstWordLength = comment.indexOf(' ')

        if (firstWordLength < 0) {
            firstWordLength = comment.length
        }

        return COMMENT_PREFIX.length + firstWordLength
    }
}
