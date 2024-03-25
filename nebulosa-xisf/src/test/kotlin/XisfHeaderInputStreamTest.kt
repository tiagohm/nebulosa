import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nebulosa.io.ByteOrder
import nebulosa.test.FitsStringSpec
import nebulosa.xisf.XisfHeaderInputStream
import nebulosa.xisf.XisfMonolithicFileHeader
import kotlin.io.path.inputStream

class XisfHeaderInputStreamTest : FitsStringSpec() {

    init {
        "read:8:gray" {
            val stream = closeAfterEach(M82_MONO_8_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT8
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.GRAY
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
        "read:8:gray:zlib" {
            val stream = closeAfterEach(M82_MONO_8_ZLIB_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT8
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.GRAY
                byteOrder shouldBe ByteOrder.LITTLE
                compressionFormat.shouldNotBeNull().type shouldBe XisfMonolithicFileHeader.CompressionType.ZLIB
                keywords shouldHaveSize 22
            }
        }
        "read:8:gray:lz4" {
            val stream = closeAfterEach(M82_MONO_8_LZ4_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT8
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.GRAY
                byteOrder shouldBe ByteOrder.LITTLE
                compressionFormat.shouldNotBeNull().type shouldBe XisfMonolithicFileHeader.CompressionType.LZ4
                keywords shouldHaveSize 22
            }
        }
        "read:8:gray:lz4-hc" {
            val stream = closeAfterEach(M82_MONO_8_LZ4_HC_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT8
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.GRAY
                byteOrder shouldBe ByteOrder.LITTLE
                compressionFormat.shouldNotBeNull().type shouldBe XisfMonolithicFileHeader.CompressionType.LZ4_HC
                keywords shouldHaveSize 22
            }
        }
        "read:8:gray:zstd" {
            val stream = closeAfterEach(M82_MONO_8_ZSTANDARD_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT8
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.GRAY
                byteOrder shouldBe ByteOrder.LITTLE
                compressionFormat.shouldNotBeNull().type shouldBe XisfMonolithicFileHeader.CompressionType.ZSTD
                keywords shouldHaveSize 22
            }
        }
        "read:16:gray" {
            val stream = closeAfterEach(M82_MONO_16_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT16
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.GRAY
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
        "read:32:gray" {
            val stream = closeAfterEach(M82_MONO_32_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT32
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.GRAY
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
        "read:F32:gray" {
            val stream = closeAfterEach(M82_MONO_F32_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.FLOAT32
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.GRAY
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
        "read:F64:gray" {
            val stream = closeAfterEach(M82_MONO_F64_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.FLOAT64
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.GRAY
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
        "read:8:rgb" {
            val stream = closeAfterEach(M82_COLOR_8_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 3
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT8
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.RGB
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
        "read:16:rgb" {
            val stream = closeAfterEach(M82_COLOR_16_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 3
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT16
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.RGB
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
        "read:32:rgb" {
            val stream = closeAfterEach(M82_COLOR_32_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 3
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.UINT32
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.RGB
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
        "read:F32:rgb" {
            val stream = closeAfterEach(M82_COLOR_F32_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 3
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.FLOAT32
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.RGB
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
        "read:F64:rgb" {
            val stream = closeAfterEach(M82_COLOR_F64_XISF.inputStream().also { it.skip(16) })
            val headerStream = XisfHeaderInputStream(stream)
            val image = headerStream.read().shouldNotBeNull().shouldBeInstanceOf<XisfMonolithicFileHeader.Image>()

            with(image) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 3
                sampleFormat shouldBe XisfMonolithicFileHeader.SampleFormat.FLOAT64
                colorSpace shouldBe XisfMonolithicFileHeader.ColorSpace.RGB
                byteOrder shouldBe ByteOrder.LITTLE
                keywords shouldHaveSize 22
            }
        }
    }
}
