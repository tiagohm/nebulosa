package nebulosa.io

import java.io.ByteArrayOutputStream
import java.util.*

class Base64OutputStream(urlSafe: Boolean = false, size: Int = 32) : ByteArrayOutputStream(size) {

    private val map = if (urlSafe) BASE64_URL_SAFE else BASE64
    private val b012 = IntArray(3)

    @Volatile private var pos = 0

    fun decoded() = String(buf, 0, count)

    @Synchronized
    override fun write(b: Int) {
        if (pos >= 3) {
            super.write(map[b012[2] and 0x3f].toInt())
            pos = 0
        }

        b012[pos] = b

        when (pos) {
            0 -> super.write(map[b and 0xff shr 2].toInt())
            1 -> super.write(map[(b012[0] and 0x03 shl 4) or (b and 0xff shr 4)].toInt())
            2 -> super.write(map[(b012[1] and 0x0f shl 2) or (b and 0xff shr 6)].toInt())
        }

        pos++
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        Objects.checkFromIndexSize(off, len, b.size)

        repeat(len) {
            write(b[off + it].toInt())
        }
    }

    override fun reset() {
        super.reset()
        pos = 0
        b012.fill(0)
    }

    @Synchronized
    fun end() {
        if (pos > 0) {
            when (pos) {
                1 -> super.write(map[b012[0] and 0x03 shl 4].toInt())
                2 -> super.write(map[b012[1] and 0x0f shl 2].toInt())
                3 -> super.write(map[b012[2] and 0x3f].toInt())
            }

            repeat(3 - pos) {
                super.write(TRAILING)
            }

            pos = 0
        }
    }

    override fun close() {
        end()
        super.close()
    }

    companion object {

        private const val TRAILING = '='.code

        private val BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".encodeToByteArray()
        private val BASE64_URL_SAFE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".encodeToByteArray()
    }
}
