import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.io.SeekableSink
import nebulosa.io.SeekableSource
import okio.Buffer

abstract class AbstractSeekableSinkAndSourceTest : StringSpec() {

    abstract val sink: SeekableSink
    abstract val source: SeekableSource

    init {
        afterEach {
            sink.seek(0)
            source.seek(0)
        }

        "byte" {
            val buffer = Buffer()
            buffer.writeByte(-12)
            sink.seek(0)
            sink.write(buffer, 1)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 1

            source.seek(0)
            source.read(buffer, 1)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 1
            buffer.readByte().toInt() shouldBeExactly -12
        }
        "short-le" {
            val buffer = Buffer()
            buffer.writeShortLe(-23975)
            sink.seek(0)
            sink.write(buffer, 2)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 2

            source.seek(0)
            source.read(buffer, 2)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 2
            buffer.readShortLe().toInt() shouldBeExactly -23975
        }
        "short" {
            val buffer = Buffer()
            buffer.writeShort(-23975)
            sink.seek(0)
            sink.write(buffer, 2)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 2

            source.seek(0)
            source.read(buffer, 2)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 2
            buffer.readShort().toInt() shouldBeExactly -23975
        }
        "int-le" {
            val buffer = Buffer()
            buffer.writeIntLe(-145983)
            sink.seek(0)
            sink.write(buffer, 4)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 4

            source.seek(0)
            source.read(buffer, 4)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 4
            buffer.readIntLe() shouldBeExactly -145983
        }
        "int" {
            val buffer = Buffer()
            buffer.writeInt(-145983)
            sink.seek(0)
            sink.write(buffer, 4)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 4

            source.seek(0)
            source.read(buffer, 4)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 4
            buffer.readInt() shouldBeExactly -145983
        }
        "long-le" {
            val buffer = Buffer()
            buffer.writeLongLe(-3534545345345)
            sink.seek(0)
            sink.write(buffer, 8)
            sink.exhausted.shouldBeTrue()
            sink.position shouldBeExactly 8

            source.seek(0)
            source.read(buffer, 8)
            source.exhausted.shouldBeTrue()
            source.position shouldBeExactly 8
            buffer.readLongLe() shouldBeExactly -3534545345345
        }
        "long" {
            val buffer = Buffer()
            buffer.writeLong(-3534545345345)
            sink.seek(0)
            sink.write(buffer, 8)
            sink.exhausted.shouldBeTrue()
            sink.position shouldBeExactly 8

            source.seek(0)
            source.read(buffer, 8)
            source.exhausted.shouldBeTrue()
            source.position shouldBeExactly 8
            buffer.readLong() shouldBeExactly -3534545345345
        }
        "ascii" {
            val buffer = Buffer()
            buffer.writeString("Gê", Charsets.ISO_8859_1)
            sink.seek(0)
            sink.write(buffer, 2)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 2

            source.seek(0)
            source.read(buffer, 2)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 2
            buffer.readString(Charsets.ISO_8859_1) shouldBe "Gê"
        }
        "utf-8" {
            val buffer = Buffer()
            buffer.writeUtf8("\uD83D\uDE0A")
            sink.seek(0)
            sink.write(buffer, 4)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 4

            source.seek(0)
            source.read(buffer, 4)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 4
            buffer.readUtf8() shouldBe "\uD83D\uDE0A"
        }
        "seek and exhausted" {
            val buffer = Buffer()
            buffer.writeByte(99)
            sink.seek(7)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 7
            sink.write(buffer, 1)
            sink.exhausted.shouldBeTrue()
            sink.position shouldBeExactly 8

            source.seek(7)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 7
            source.read(buffer, 1)
            source.exhausted.shouldBeTrue()
            source.position shouldBeExactly 8
            buffer.readByte() shouldBe 99
        }
        "seek and not exhausted" {
            val buffer = Buffer()
            buffer.writeByte(99)
            sink.seek(6)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 6
            sink.write(buffer, 1)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 7

            source.seek(6)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 6
            source.read(buffer, 1)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 7
            buffer.readByte() shouldBe 99
        }
        "negative seek and exhausted" {
            val buffer = Buffer()
            buffer.writeByte(99)
            sink.seek(-1)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 7
            sink.write(buffer, 1)
            sink.exhausted.shouldBeTrue()
            sink.position shouldBeExactly 8

            source.seek(-1)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 7
            source.read(buffer, 1)
            source.exhausted.shouldBeTrue()
            source.position shouldBeExactly 8
            buffer.readByte() shouldBe 99
        }
        "negative seek and not exhausted" {
            val buffer = Buffer()
            buffer.writeByte(99)
            sink.seek(-2)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 6
            sink.write(buffer, 1)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 7

            source.seek(-2)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 6
            source.read(buffer, 1)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 7
            buffer.readByte() shouldBe 99
        }
        "skip and exhausted" {
            val buffer = Buffer()
            buffer.writeByte(99)
            sink.skip(7)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 7
            sink.write(buffer, 1)
            sink.exhausted.shouldBeTrue()
            sink.position shouldBeExactly 8

            source.skip(7)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 7
            source.read(buffer, 1)
            source.exhausted.shouldBeTrue()
            source.position shouldBeExactly 8
            buffer.readByte() shouldBe 99
        }
        "skip and not exhausted" {
            val buffer = Buffer()
            buffer.writeByte(99)
            sink.skip(6)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 6
            sink.write(buffer, 1)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 7

            source.skip(6)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 6
            source.read(buffer, 1)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 7
            buffer.readByte() shouldBe 99
        }
        "negative skip and exhausted" {
            val buffer = Buffer()
            buffer.writeByte(99)
            sink.skip(-1)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 7
            sink.write(buffer, 1)
            sink.exhausted.shouldBeTrue()
            sink.position shouldBeExactly 8

            source.skip(-1)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 7
            source.read(buffer, 1)
            source.exhausted.shouldBeTrue()
            source.position shouldBeExactly 8
            buffer.readByte() shouldBe 99
        }
        "negative skip and not exhausted" {
            val buffer = Buffer()
            buffer.writeByte(99)
            sink.skip(-2)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 6
            sink.write(buffer, 1)
            sink.exhausted.shouldBeFalse()
            sink.position shouldBeExactly 7

            source.skip(-2)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 6
            source.read(buffer, 1)
            source.exhausted.shouldBeFalse()
            source.position shouldBeExactly 7
            buffer.readByte() shouldBe 99
        }
    }
}
