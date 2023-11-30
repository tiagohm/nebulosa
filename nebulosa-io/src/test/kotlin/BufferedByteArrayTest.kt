import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
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

class BufferedByteArrayTest : StringSpec() {

    init {
        val data = ByteArray(79)

        "write" {
            val sink = data.sink()

            val buffer = sink.buffer()
            buffer.writeByte(0xab)
            buffer.writeShort(0xabcd)
            buffer.writeShortLe(0x2143)
            buffer.writeInt(-0x543210ff)
            buffer.writeIntLe(-0x789abcdf)
            buffer.writeLong(-0x543210fe789abcdfL)
            buffer.writeLongLe(-0x350145414f4ea400L)
            buffer.writeFloat(3.14f)
            buffer.writeFloatLe(3.14f)
            buffer.writeDouble(3.14)
            buffer.writeDoubleLe(3.14)
            buffer.writeUtf8("təˈranəˌsôr")
            buffer.writeUtf8CodePoint("µ".codePointAt(0))
            buffer.writeString("c", charset = Charsets.UTF_32BE)
            buffer.write(byteArrayOf(1, 2, 3))
            buffer.writeByte(0xf9)
            buffer.flush()
        }
        "read" {
            val source = data.source()

            val buffer = source.buffer()
            buffer.readUnsignedByte() shouldBeExactly 0xab
            buffer.readShort().toInt() and 0xffff shouldBeExactly 0xabcd
            buffer.readShortLe().toInt() and 0xffff shouldBeExactly 0x2143
            buffer.readInt() shouldBeExactly -0x543210ff
            buffer.readIntLe() shouldBeExactly -0x789abcdf
            buffer.readLong() shouldBeExactly -0x543210fe789abcdfL
            buffer.readLongLe() shouldBeExactly -0x350145414f4ea400L
            buffer.readFloat() shouldBeExactly 3.14f
            buffer.readFloatLe() shouldBeExactly 3.14f
            buffer.readDouble() shouldBeExactly 3.14
            buffer.readDoubleLe() shouldBeExactly 3.14
            buffer.readUtf8("təˈranəˌsôr".utf8Size()) shouldBe "təˈranəˌsôr"
            buffer.readUtf8CodePoint() shouldBeExactly "µ".codePointAt(0)
            buffer.readString(4, Charsets.UTF_32BE) shouldBe "c"
            buffer.readByteArray(3) shouldBe byteArrayOf(1, 2, 3)
            buffer.exhausted().shouldBeFalse()
            buffer.readUnsignedByte() shouldBeExactly 0xf9
            buffer.exhausted().shouldBeTrue()
        }
        "seek and write" {
            val sink = data.sink()
            sink.seek(-1L)

            val buffer = sink.buffer()
            buffer.writeByte(0x44)
            buffer.flush()
        }
        "skip and read" {
            val source = data.source()
            source.skip(78)

            val buffer = source.buffer()
            buffer.exhausted().shouldBeFalse()
            buffer.readSignedByte() shouldBeExactly 0x44
            buffer.exhausted().shouldBeTrue()
        }
        "seek and read" {
            val source = data.source()
            source.seek(-1L)

            val buffer = source.buffer()
            buffer.exhausted().shouldBeFalse()
            buffer.readSignedByte() shouldBeExactly 0x44
            buffer.exhausted().shouldBeTrue()
        }
        "write with offset and byte count" {
            val sink = data.sink(2, 8)

            val buffer = sink.buffer()
            buffer.writeDouble(3.14)
            shouldNotThrow<EOFException> { buffer.flush() }

            buffer.writeByte(0x10)
            shouldThrow<EOFException> { buffer.flush() }
        }
        "read with offset and byte count" {
            val source = data.source(2, 8)

            val buffer = source.buffer()
            buffer.exhausted().shouldBeFalse()
            buffer.readDouble() shouldBeExactly 3.14
            buffer.exhausted().shouldBeTrue()
        }
        "close emits buffered bytes" {
            data.fill(0)

            val sink = data.sink()

            sink.buffer().use {
                it.writeByte(0x99)
            }

            val source = data.source()
            val buffer = source.buffer()
            buffer.readUnsignedByte() shouldBeExactly 0x99
        }
    }
}
