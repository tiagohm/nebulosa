import io.kotest.engine.spec.tempfile
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
import java.io.File

class BufferedRandomAccessFileTest : BufferedStringSpec() {

    init {
        "read" {
            val file = makeFile()
            val source = file.seekableSource()

            with(source.buffer()) {
                readUnsignedByte() shouldBeExactly 0xab
                readShort().toInt() and 0xffff shouldBeExactly 0xabcd
                readShortLe().toInt() and 0xffff shouldBeExactly 0x2143
                readInt() shouldBeExactly -0x543210ff
                readIntLe() shouldBeExactly -0x789abcdf
                readLong() shouldBeExactly -0x543210fe789abcdfL
                readLongLe() shouldBeExactly -0x350145414f4ea400L
                readFloat() shouldBeExactly 3.14f
                readFloatLe() shouldBeExactly 3.14f
                readDouble() shouldBeExactly 3.14
                readDoubleLe() shouldBeExactly 3.14
                readUtf8("təˈranəˌsôr".utf8Size()) shouldBe "təˈranəˌsôr"
                readUtf8CodePoint() shouldBeExactly "µ".codePointAt(0)
                readString(4, Charsets.UTF_32BE) shouldBe "c"
                readByteArray(3) shouldBe byteArrayOf(1, 2, 3)
                exhausted().shouldBeFalse()
                readUnsignedByte() shouldBeExactly 0xf9
                exhausted().shouldBeTrue()
            }
        }
        "seek and write" {
            val file = makeFile()
            val sink = file.seekableSink()
            sink.seek(-1L)

            with(sink.buffer()) {
                writeByte(0x44)
                flush()
            }

            val source = file.seekableSource()
            source.seek(-1L)

            with(source.buffer()) {
                readByte().toInt() shouldBeExactly 0x44
            }
        }
        "skip and read" {
            val file = makeFile()
            val source = file.seekableSource()
            source.skip(78)

            with(source.buffer()) {
                exhausted().shouldBeFalse()
                readSignedByte() shouldBeExactly 0xF9.toByte().toInt()
                exhausted().shouldBeTrue()
            }
        }
        "seek and read" {
            val file = makeFile()
            val source = file.seekableSource()

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
        "close emits buffered bytes" {
            val file = makeFile()
            file.writeBytes(ByteArray(79) { 1 })

            val sink = file.seekableSink()
            sink.buffer().use { it.writeByte(0x99) }

            file.seekableSource().buffer().use {
                it.readUnsignedByte() shouldBeExactly 0x99
                it.readUnsignedByte() shouldBeExactly 1
            }
        }
    }

    private fun makeFile(): File {
        val file = tempfile()
        file.seekableSink().use { it.initialize() }
        return file
    }
}
