package nebulosa.io

import java.io.InputStream

class Base64InputStream(private val input: String) : InputStream() {

    private var state = -1
    private var pos = 0
    private var limit = 0

    private val buffer = IntArray(3)

    init {
        // Ignore trailing '=' padding and whitespace from the input.
        limit = input.length

        while (limit > 0) {
            val c = input[limit - 1]

            if (c != '=' && c != '\n' && c != '\r' && c != ' ' && c != '\t') {
                break
            }

            limit--
        }
    }

    override fun read() = decode()

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (pos >= limit) return -1

        for (i in 0 until len) {
            val read = decode()
            if (read == -1) return i
            b[off + i] = read.toByte()
        }

        return len
    }

    private fun decode(): Int {
        if (state in 0..2) {
            return buffer[state++]
        } else if (pos >= limit) {
            return -1
        }

        buffer.fill(-1)
        state = 0

        var inCount = 0
        var word = 0

        while (pos < limit) {
            val c = input[pos++]

            val bits = if (c in 'A'..'Z') {
                // char ASCII value
                //  A    65    0
                //  Z    90    25 (ASCII - 65)
                c.code - 65
            } else if (c in 'a'..'z') {
                // char ASCII value
                //  a    97    26
                //  z    122   51 (ASCII - 71)
                c.code - 71
            } else if (c in '0'..'9') {
                // char ASCII value
                //  0    48    52
                //  9    57    61 (ASCII + 4)
                c.code + 4
            } else if (c == '+' || c == '-') {
                62
            } else if (c == '/' || c == '_') {
                63
            } else if (c == '\n' || c == '\r' || c == ' ' || c == '\t') {
                continue
            } else {
                throw IllegalStateException("unrecognized input: $c")
            }

            // Append this char's 6 bits to the word.
            word = word shl 6 or bits

            // For every 4 chars of input, we accumulate 24 bits of output. Emit 3 bytes.
            inCount++

            if (inCount % 4 == 0) {
                buffer[0] = (word shr 16) and 0xff
                buffer[1] = (word shr 8) and 0xff
                buffer[2] = word and 0xff
                break
            }
        }

        when (inCount % 4) {
            1 -> {
                // We read 1 char followed by "===". But 6 bits is a truncated byte! Fail.
                throw IllegalStateException("truncated byte")
            }
            2 -> {
                // We read 2 chars followed by "==". Emit 1 byte with 8 of those 12 bits.
                word = word shl 12
                buffer[0] = (word shr 16) and 0xff
            }
            3 -> {
                // We read 3 chars, followed by "=". Emit 2 bytes for 16 of those 18 bits.
                word = word shl 6
                buffer[0] = (word shr 16) and 0xff
                buffer[1] = (word shr 8) and 0xff
            }
        }

        return buffer[state++]
    }

    override fun markSupported() = false
}
