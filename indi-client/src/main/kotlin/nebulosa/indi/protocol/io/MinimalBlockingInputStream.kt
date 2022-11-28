package nebulosa.indi.protocol.io

import java.io.InputStream
import kotlin.math.min

class MinimalBlockingInputStream(private val input: InputStream) : InputStream() {

    override fun read() = input.read()

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return if (len > 0) {
            // We want to minimize blocking so we will only read as much as is available.
            if (input.available() > 0) {
                input.read(b, off, min(input.available(), len))
            } else {
                // We wait for one byte to come.
                val count = input.read(b, off, 1)
                // Maybe there is more than one byte available now?
                if (input.available() > 0) {
                    input.read(b, off + count, min(input.available(), len - count)) + count
                } else {
                    count
                }
            }
        } else {
            0
        }
    }

    override fun available() = input.available()

    override fun skip(n: Long) = input.skip(n)

    override fun markSupported() = input.markSupported()

    override fun mark(readlimit: Int) = input.mark(readlimit)

    override fun reset() = input.reset()

    override fun close() = input.close()
}
