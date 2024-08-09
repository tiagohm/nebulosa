import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import nebulosa.fits.FitsFormat
import nebulosa.fits.bitpix
import nebulosa.image.format.ImageHdu
import nebulosa.image.format.ImageHdu.Companion.makeImage
import nebulosa.io.seekableSink
import nebulosa.io.seekableSource
import nebulosa.test.*
import nebulosa.xisf.XisfFormat
import nebulosa.xisf.isXisf
import org.junit.jupiter.api.Test

class XisfFormatTest : AbstractTest() {

    @Test
    fun shouldBeXisfFormat() {
        NGC3344_COLOR_8_FITS.isXisf().shouldBeFalse()
        M82_COLOR_16_XISF.isXisf().shouldBeTrue()
        tempPath("empty-", ".xisf").isXisf().shouldBeFalse()
    }

    @Test
    fun monoPlanar8Bit() {
        val source = M82_MONO_8_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 1
            header shouldHaveSize 23
            data.red.size shouldBeExactly 512 * 512
            data.green shouldBeSameInstanceAs data.red
            data.blue shouldBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-mono-planar-8").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
        }
    }

    @Test
    fun monoPlanar16Bit() {
        val source = M82_MONO_16_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 1
            header shouldHaveSize 23
            data.red.size shouldBeExactly 512 * 512
            data.green shouldBeSameInstanceAs data.red
            data.blue shouldBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-mono-planar-16").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
        }
    }

    @Test
    fun monoPlanar32Bit() {
        val source = M82_MONO_32_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 1
            header shouldHaveSize 23
            data.red.size shouldBeExactly 512 * 512
            data.green shouldBeSameInstanceAs data.red
            data.blue shouldBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-mono-planar-32").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
        }
    }

    @Test
    fun monoPlanarFloat32Bit() {
        val source = M82_MONO_F32_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 1
            header shouldHaveSize 23
            data.red.size shouldBeExactly 512 * 512
            data.green shouldBeSameInstanceAs data.red
            data.blue shouldBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-mono-planar-F32").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
        }
    }

    @Test
    fun monoPlanarFloat64() {
        val source = M82_MONO_F64_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 1
            header shouldHaveSize 23
            data.red.size shouldBeExactly 512 * 512
            data.green shouldBeSameInstanceAs data.red
            data.blue shouldBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-mono-planar-F64").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
        }
    }

    @Test
    fun colorPlanar8Bit() {
        val source = M82_COLOR_8_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 3
            header shouldHaveSize 24
            data.red.size shouldBeExactly 512 * 512
            data.green shouldNotBeSameInstanceAs data.red
            data.blue shouldNotBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-color-planar-8").second shouldBe "764e326cc5260d81f3761112ad6a1969"
        }
    }

    @Test
    fun colorPlanar16Bit() {
        val source = M82_COLOR_16_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 3
            header shouldHaveSize 24
            data.red.size shouldBeExactly 512 * 512
            data.green shouldNotBeSameInstanceAs data.red
            data.blue shouldNotBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-color-planar-16").second shouldBe "764e326cc5260d81f3761112ad6a1969"
        }
    }

    @Test
    fun colorPlanar32Bit() {
        val source = M82_COLOR_32_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 3
            header shouldHaveSize 24
            data.red.size shouldBeExactly 512 * 512
            data.green shouldNotBeSameInstanceAs data.red
            data.blue shouldNotBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-color-planar-32").second shouldBe "764e326cc5260d81f3761112ad6a1969"
        }
    }

    @Test
    fun colorPlanarFloat32Bit() {
        val source = M82_COLOR_F32_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 3
            header shouldHaveSize 24
            data.red.size shouldBeExactly 512 * 512
            data.green shouldNotBeSameInstanceAs data.red
            data.blue shouldNotBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-color-planar-F32").second shouldBe "764e326cc5260d81f3761112ad6a1969"
        }
    }

    @Test
    fun colorPlanarFloat64() {
        val source = M82_COLOR_F64_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 3
            header shouldHaveSize 24
            data.red.size shouldBeExactly 512 * 512
            data.green shouldNotBeSameInstanceAs data.red
            data.blue shouldNotBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-color-planar-F64").second shouldBe "764e326cc5260d81f3761112ad6a1969"
        }
    }

    @Test
    fun monoPlanar8BitZLib() {
        val source = M82_MONO_8_ZLIB_XISF.seekableSource().autoClose()
        val hdus = XisfFormat.read(source)

        hdus shouldHaveSize 1

        with(hdus[0]) {
            width shouldBeExactly 512
            height shouldBeExactly 512
            numberOfChannels shouldBeExactly 1
            header shouldHaveSize 23
            data.red.size shouldBeExactly 512 * 512
            data.green shouldBeSameInstanceAs data.red
            data.blue shouldBeSameInstanceAs data.green

            val image = makeImage()
            image.save("xisf-mono-planar-8-zlib").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
        }
    }

    @Test
    fun monoWrite() {
        val formats = arrayOf(PALETTE_MONO_8_XISF, PALETTE_MONO_16_XISF, PALETTE_MONO_32_XISF, PALETTE_MONO_F32_XISF, PALETTE_MONO_F64_XISF)

        for (format in formats) {
            val source0 = format.seekableSource().autoClose()
            val hdus0 = XisfFormat.read(source0)

            val outputPath = tempPath("mono-", ".xisf")
            val sink = outputPath.seekableSink().autoClose()
            XisfFormat.write(sink, hdus0)

            val source1 = outputPath.seekableSource().autoClose()
            val hdus1 = XisfFormat.read(source1)

            hdus1 shouldHaveSize 1
            hdus1[0].data.numberOfChannels shouldBeExactly 1
            hdus1[0].header.size shouldBeExactly hdus0[0].header.size
            hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
            hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
            hdus1[0].data.red shouldBe hdus0[0].data.red

            val bitpix = hdus1[0].header.bitpix
            val image = hdus1[0].makeImage()
            image.save("xisf-mono-write-$bitpix").second shouldBe "07762064ff54ccc7771ba5b34fca86cf"
        }
    }

    @Test
    fun colorWrite() {
        val formats = arrayOf(PALETTE_COLOR_8_XISF, PALETTE_COLOR_16_XISF, PALETTE_COLOR_32_XISF, PALETTE_COLOR_F32_XISF, PALETTE_COLOR_F64_XISF)

        for (format in formats) {
            val source0 = format.seekableSource().autoClose()
            val hdus0 = XisfFormat.read(source0)

            val outputPath = tempPath("color-", ".xisf")
            val sink = outputPath.seekableSink().autoClose()
            XisfFormat.write(sink, hdus0)

            val source1 = outputPath.seekableSource().autoClose()
            val hdus1 = XisfFormat.read(source1)

            hdus1 shouldHaveSize 1
            hdus1[0].data.numberOfChannels shouldBeExactly 3
            hdus1[0].header.size shouldBeExactly hdus0[0].header.size
            hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
            hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
            hdus1[0].data.red shouldBe hdus0[0].data.red
            hdus1[0].data.green shouldNotBeSameInstanceAs hdus0[0].data.green
            hdus1[0].data.green shouldBe hdus0[0].data.green
            hdus1[0].data.blue shouldNotBeSameInstanceAs hdus0[0].data.blue
            hdus1[0].data.blue shouldBe hdus0[0].data.blue

            val bitpix = hdus1[0].header.bitpix
            val image = hdus1[0].makeImage()
            image.save("xisf-color-write-$bitpix").second shouldBe "7233886f62065800b43419f3b1b6c833"
        }
    }

    @Test
    fun fitsToXisfMono() {
        val formats = arrayOf(PALETTE_MONO_8_FITS, PALETTE_MONO_16_FITS, PALETTE_MONO_32_FITS, PALETTE_MONO_F32_FITS, PALETTE_MONO_F64_FITS)

        for (format in formats) {
            val source0 = format.seekableSource().autoClose()
            val hdus0 = FitsFormat.read(source0).filterIsInstance<ImageHdu>()

            hdus0 shouldHaveSize 1

            val outputPath = tempPath("fits-xisf-mono-", ".xisf")
            val sink = outputPath.seekableSink().autoClose()
            XisfFormat.write(sink, hdus0)

            val source1 = outputPath.seekableSource().autoClose()
            val hdus1 = XisfFormat.read(source1)

            hdus1 shouldHaveSize 1
            hdus1[0].data.numberOfChannels shouldBeExactly 1
            // hdus1[0].header.size shouldBeExactly hdus0[0].header.size
            hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
            hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
            hdus1[0].data.red shouldBe hdus0[0].data.red

            val bitpix = hdus1[0].header.bitpix
            val image = hdus1[0].makeImage()
            image.save("fits-to-xisf-mono-$bitpix").second shouldBe "07762064ff54ccc7771ba5b34fca86cf"
        }
    }

    @Test
    fun fitsToXisfColor() {
        val formats = arrayOf(PALETTE_COLOR_8_FITS, PALETTE_COLOR_16_FITS, PALETTE_COLOR_32_FITS, PALETTE_COLOR_F32_FITS, PALETTE_COLOR_F64_FITS)

        for (format in formats) {
            val source0 = format.seekableSource().autoClose()
            val hdus0 = FitsFormat.read(source0).filterIsInstance<ImageHdu>()

            hdus0 shouldHaveSize 1

            val outputPath = tempPath("fits-xisf-color", ".xisf")
            val sink = outputPath.seekableSink().autoClose()
            XisfFormat.write(sink, hdus0)

            val source1 = outputPath.seekableSource().autoClose()
            val hdus1 = XisfFormat.read(source1)

            hdus1 shouldHaveSize 1
            hdus1[0].data.numberOfChannels shouldBeExactly 3
            // hdus1[0].header.size shouldBeExactly hdus0[0].header.size
            hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
            hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
            hdus1[0].data.red shouldBe hdus0[0].data.red
            hdus1[0].data.green shouldNotBeSameInstanceAs hdus0[0].data.green
            hdus1[0].data.green shouldBe hdus0[0].data.green
            hdus1[0].data.blue shouldNotBeSameInstanceAs hdus0[0].data.blue
            hdus1[0].data.blue shouldBe hdus0[0].data.blue

            val bitpix = hdus1[0].header.bitpix
            val image = hdus1[0].makeImage()
            image.save("fits-to-xisf-color-$bitpix").second shouldBe "7233886f62065800b43419f3b1b6c833"
        }
    }

    @Test
    fun xisfToFitsMono() {
        val formats = arrayOf(PALETTE_MONO_8_XISF, PALETTE_MONO_16_XISF, PALETTE_MONO_32_XISF, PALETTE_MONO_F32_XISF, PALETTE_MONO_F64_XISF)

        for (format in formats) {
            val source0 = format.seekableSource().autoClose()
            val hdus0 = XisfFormat.read(source0)

            hdus0 shouldHaveSize 1

            val outputPath = tempPath("xisf-fits-mono", ".fits")
            val sink = outputPath.seekableSink().autoClose()
            FitsFormat.write(sink, hdus0)

            val source1 = outputPath.seekableSource().autoClose()
            val hdus1 = FitsFormat.read(source1).filterIsInstance<ImageHdu>()

            hdus1 shouldHaveSize 1
            hdus1[0].data.numberOfChannels shouldBeExactly 1
            // hdus1[0].header.size shouldBeExactly hdus0[0].header.size
            hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
            hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
            hdus1[0].data.red shouldBe hdus0[0].data.red

            val bitpix = hdus1[0].header.bitpix
            val image = hdus1[0].makeImage()
            image.save("xisf-to-fits-mono-$bitpix").second shouldBe "07762064ff54ccc7771ba5b34fca86cf"
        }
    }

    @Test
    fun xisfToFitsColor() {
        val formats = arrayOf(PALETTE_COLOR_8_XISF, PALETTE_COLOR_16_XISF, PALETTE_COLOR_32_XISF, PALETTE_COLOR_F32_XISF, PALETTE_COLOR_F64_XISF)

        for (format in formats) {
            val source0 = format.seekableSource().autoClose()
            val hdus0 = XisfFormat.read(source0)

            hdus0 shouldHaveSize 1

            val outputPath = tempPath("xisf-fits-color", ".fits")
            val sink = outputPath.seekableSink().autoClose()
            FitsFormat.write(sink, hdus0)

            val source1 = outputPath.seekableSource().autoClose()
            val hdus1 = FitsFormat.read(source1).filterIsInstance<ImageHdu>()

            hdus1 shouldHaveSize 1
            hdus1[0].data.numberOfChannels shouldBeExactly 3
            // hdus1[0].header.size shouldBeExactly hdus0[0].header.size
            hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
            hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
            hdus1[0].data.red shouldBe hdus0[0].data.red
            hdus1[0].data.green shouldNotBeSameInstanceAs hdus0[0].data.green
            hdus1[0].data.green shouldBe hdus0[0].data.green
            hdus1[0].data.blue shouldNotBeSameInstanceAs hdus0[0].data.blue
            hdus1[0].data.blue shouldBe hdus0[0].data.blue

            val bitpix = hdus1[0].header.bitpix
            val image = hdus1[0].makeImage()
            image.save("xisf-to-fits-color-$bitpix").second shouldBe "7233886f62065800b43419f3b1b6c833"
        }
    }
}
