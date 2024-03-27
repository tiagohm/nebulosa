import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.io.*
import okio.buffer
import okio.utf8Size
import java.io.EOFException
import java.nio.ByteBuffer

class BufferedByteBufferTest : BufferedStringSpec() {

    init {
        "read" {
            val data = ByteBuffer.allocate(79)
            data.sink().use { it.initialize() }

            val source = data.source()

            with(source.buffer()) {
                readUnsignedByte() shouldBeExactly 0xAB
                readShort().toInt() and 0xFFFF shouldBeExactly 0XABCD
                readShortLe().toInt() and 0xFFFF shouldBeExactly 0X2143
                readInt() shouldBeExactly -0x543210FF
                readIntLe() shouldBeExactly -0x789ABCDF
                readLong() shouldBeExactly -0x543210FE789ABCDFL
                readLongLe() shouldBeExactly -0x350145414F4EA400L
                readFloat() shouldBeExactly 3.14F
                readFloatLe() shouldBeExactly 3.14F
                readDouble() shouldBeExactly 3.14
                readDoubleLe() shouldBeExactly 3.14
                readUtf8("təˈranəˌsôr".utf8Size()) shouldBe "təˈranəˌsôr"
                readUtf8CodePoint() shouldBeExactly "µ".codePointAt(0)
                readString(4, Charsets.UTF_32BE) shouldBe "c"
                readByteArray(3) shouldBe byteArrayOf(1, 2, 3)
                exhausted().shouldBeFalse()
                readUnsignedByte() shouldBeExactly 0xF9
                exhausted().shouldBeTrue()
            }
        }
        "seek and write" {
            val data = ByteBuffer.allocate(79)

            val sink = data.sink()
            sink.initialize()
            sink.seek(-1L)

            with(sink.buffer()) {
                writeByte(0x44)
                flush()
            }
        }
        "skip and read" {
            val data = ByteBuffer.allocate(79)
            data.sink().use { it.initialize() }

            val source = data.source()
            source.skip(78)

            with(source.buffer()) {
                exhausted().shouldBeFalse()
                readSignedByte() shouldBeExactly 0xF9.toByte().toInt()
                exhausted().shouldBeTrue()
            }
        }
        "seek and read" {
            val data = ByteBuffer.allocate(79)
            data.sink().use { it.initialize() }

            val source = data.source()

            with(source.buffer()) {
                source.seek(-1L)
                exhausted().shouldBeFalse()
                readSignedByte() shouldBeExactly 0xF9.toByte().toInt()
                exhausted().shouldBeTrue()

                source.seek(37)
                exhausted().shouldBeFalse()
                readDouble() shouldBeExactly 3.14
                exhausted().shouldBeFalse()
            }
        }
        "write with offset and byte count" {
            val data = ByteBuffer.allocate(79)
            data.sink().use { it.initialize() }

            val sink = data.sink(2, 8)

            with(sink.buffer()) {
                writeDouble(3.14)
                shouldNotThrow<EOFException> { flush() }

                writeByte(0x10)
                shouldThrow<EOFException> { flush() }
            }
        }
        "read with offset and byte count" {
            val data = ByteBuffer.allocate(79)
            data.sink().use { it.initialize() }

            val source = data.source(37, 8)

            with(source.buffer()) {
                exhausted().shouldBeFalse()
                readDouble() shouldBeExactly 3.14
                exhausted().shouldBeTrue()
            }
        }
        "close emits buffered bytes" {
            val data = ByteBuffer.allocate(79)
            repeat(data.capacity()) { data.put(1) }

            val sink = data.sink()
            sink.buffer().use { it.writeByte(0x99) }

            data.source().buffer().use {
                it.readUnsignedByte() shouldBeExactly 0x99
                it.readUnsignedByte() shouldBeExactly 1
            }
        }
    }
}
