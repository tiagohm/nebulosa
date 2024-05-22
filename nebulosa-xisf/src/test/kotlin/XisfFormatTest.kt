import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import nebulosa.fits.FitsFormat
import nebulosa.fits.bitpix
import nebulosa.image.format.ImageHdu
import nebulosa.io.seekableSink
import nebulosa.io.seekableSource
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.xisf.XisfFormat
import nebulosa.xisf.isXisf

class XisfFormatTest : AbstractFitsAndXisfTest() {

    init {
        "should be xisf format" {
            NGC3344_COLOR_8_FITS.isXisf().shouldBeFalse()
            M82_COLOR_16_XISF.isXisf().shouldBeTrue()
        }
        "mono:planar:8" {
            val source = closeAfterEach(M82_MONO_8_XISF.seekableSource())
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
        "mono:planar:16" {
            val source = closeAfterEach(M82_MONO_16_XISF.seekableSource())
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
        "mono:planar:32" {
            val source = closeAfterEach(M82_MONO_32_XISF.seekableSource())
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
        "mono:planar:F32" {
            val source = closeAfterEach(M82_MONO_F32_XISF.seekableSource())
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
        "mono:planar:F64" {
            val source = closeAfterEach(M82_MONO_F64_XISF.seekableSource())
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
        "color:planar:8" {
            val source = closeAfterEach(M82_COLOR_8_XISF.seekableSource())
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
        "color:planar:16" {
            val source = closeAfterEach(M82_COLOR_16_XISF.seekableSource())
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
        "color:planar:32" {
            val source = closeAfterEach(M82_COLOR_32_XISF.seekableSource())
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
        "color:planar:F32" {
            val source = closeAfterEach(M82_COLOR_F32_XISF.seekableSource())
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
        "color:planar:F64" {
            val source = closeAfterEach(M82_COLOR_F64_XISF.seekableSource())
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
        "mono:planar:8:zlib" {
            val source = closeAfterEach(M82_MONO_8_ZLIB_XISF.seekableSource())
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
        "mono:write" {
            val formats = arrayOf(PALETTE_MONO_8_XISF, PALETTE_MONO_16_XISF, PALETTE_MONO_32_XISF, PALETTE_MONO_F32_XISF, PALETTE_MONO_F64_XISF)

            for (format in formats) {
                val source0 = closeAfterEach(format.seekableSource())
                val hdus0 = XisfFormat.read(source0)

                val outputPath = tempfile()
                val sink = closeAfterEach(outputPath.seekableSink())
                XisfFormat.write(sink, hdus0)

                val source1 = closeAfterEach(outputPath.seekableSource())
                val hdus1 = XisfFormat.read(source1)

                hdus1 shouldHaveSize 1
                hdus1[0].data.numberOfChannels shouldBeExactly 1
                hdus1[0].header.size shouldBeExactly hdus0[0].header.size
                hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
                hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
                hdus1[0].data.red.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.red[i] }

                val bitpix = hdus1[0].header.bitpix
                val image = hdus1[0].makeImage()
                image.save("xisf-mono-write-$bitpix").second shouldBe "07762064ff54ccc7771ba5b34fca86cf"
            }
        }
        "color:write" {
            val formats = arrayOf(PALETTE_COLOR_8_XISF, PALETTE_COLOR_16_XISF, PALETTE_COLOR_32_XISF, PALETTE_COLOR_F32_XISF, PALETTE_COLOR_F64_XISF)

            for (format in formats) {
                val source0 = closeAfterEach(format.seekableSource())
                val hdus0 = XisfFormat.read(source0)

                val outputPath = tempfile()
                val sink = closeAfterEach(outputPath.seekableSink())
                XisfFormat.write(sink, hdus0)

                val source1 = closeAfterEach(outputPath.seekableSource())
                val hdus1 = XisfFormat.read(source1)

                hdus1 shouldHaveSize 1
                hdus1[0].data.numberOfChannels shouldBeExactly 3
                hdus1[0].header.size shouldBeExactly hdus0[0].header.size
                hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
                hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
                hdus1[0].data.red.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.red[i] }
                hdus1[0].data.green shouldNotBeSameInstanceAs hdus0[0].data.green
                hdus1[0].data.green.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.green[i] }
                hdus1[0].data.blue shouldNotBeSameInstanceAs hdus0[0].data.blue
                hdus1[0].data.blue.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.blue[i] }

                val bitpix = hdus1[0].header.bitpix
                val image = hdus1[0].makeImage()
                image.save("xisf-color-write-$bitpix").second shouldBe "7233886f62065800b43419f3b1b6c833"
            }
        }
        "fits-to-xisf:mono" {
            val formats = arrayOf(PALETTE_MONO_8_FITS, PALETTE_MONO_16_FITS, PALETTE_MONO_32_FITS, PALETTE_MONO_F32_FITS, PALETTE_MONO_F64_FITS)

            for (format in formats) {
                val source0 = closeAfterEach(format.seekableSource())
                val hdus0 = FitsFormat.read(source0).filterIsInstance<ImageHdu>()

                hdus0 shouldHaveSize 1

                val outputPath = tempfile()
                val sink = closeAfterEach(outputPath.seekableSink())
                XisfFormat.write(sink, hdus0)

                val source1 = closeAfterEach(outputPath.seekableSource())
                val hdus1 = XisfFormat.read(source1)

                hdus1 shouldHaveSize 1
                hdus1[0].data.numberOfChannels shouldBeExactly 1
                // hdus1[0].header.size shouldBeExactly hdus0[0].header.size
                hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
                hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
                hdus1[0].data.red.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.red[i] }

                val bitpix = hdus1[0].header.bitpix
                val image = hdus1[0].makeImage()
                image.save("fits-to-xisf-mono-$bitpix").second shouldBe "07762064ff54ccc7771ba5b34fca86cf"
            }
        }
        "fits-to-xisf:color" {
            val formats = arrayOf(PALETTE_COLOR_8_FITS, PALETTE_COLOR_16_FITS, PALETTE_COLOR_32_FITS, PALETTE_COLOR_F32_FITS, PALETTE_COLOR_F64_FITS)

            for (format in formats) {
                val source0 = closeAfterEach(format.seekableSource())
                val hdus0 = FitsFormat.read(source0).filterIsInstance<ImageHdu>()

                hdus0 shouldHaveSize 1

                val outputPath = tempfile()
                val sink = closeAfterEach(outputPath.seekableSink())
                XisfFormat.write(sink, hdus0)

                val source1 = closeAfterEach(outputPath.seekableSource())
                val hdus1 = XisfFormat.read(source1)

                hdus1 shouldHaveSize 1
                hdus1[0].data.numberOfChannels shouldBeExactly 3
                // hdus1[0].header.size shouldBeExactly hdus0[0].header.size
                hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
                hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
                hdus1[0].data.red.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.red[i] }
                hdus1[0].data.green shouldNotBeSameInstanceAs hdus0[0].data.green
                hdus1[0].data.green.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.green[i] }
                hdus1[0].data.blue shouldNotBeSameInstanceAs hdus0[0].data.blue
                hdus1[0].data.blue.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.blue[i] }

                val bitpix = hdus1[0].header.bitpix
                val image = hdus1[0].makeImage()
                image.save("fits-to-xisf-color-$bitpix").second shouldBe "7233886f62065800b43419f3b1b6c833"
            }
        }
        "xisf-to-fits:mono" {
            val formats = arrayOf(PALETTE_MONO_8_XISF, PALETTE_MONO_16_XISF, PALETTE_MONO_32_XISF, PALETTE_MONO_F32_XISF, PALETTE_MONO_F64_XISF)

            for (format in formats) {
                val source0 = closeAfterEach(format.seekableSource())
                val hdus0 = XisfFormat.read(source0)

                hdus0 shouldHaveSize 1

                val outputPath = tempfile()
                val sink = closeAfterEach(outputPath.seekableSink())
                FitsFormat.write(sink, hdus0)

                val source1 = closeAfterEach(outputPath.seekableSource())
                val hdus1 = FitsFormat.read(source1).filterIsInstance<ImageHdu>()

                hdus1 shouldHaveSize 1
                hdus1[0].data.numberOfChannels shouldBeExactly 1
                // hdus1[0].header.size shouldBeExactly hdus0[0].header.size
                hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
                hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
                hdus1[0].data.red.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.red[i] }

                val bitpix = hdus1[0].header.bitpix
                val image = hdus1[0].makeImage()
                image.save("xisf-to-fits-mono-$bitpix").second shouldBe "07762064ff54ccc7771ba5b34fca86cf"
            }
        }
        "xisf-to-fits:color" {
            val formats = arrayOf(PALETTE_COLOR_8_XISF, PALETTE_COLOR_16_XISF, PALETTE_COLOR_32_XISF, PALETTE_COLOR_F32_XISF, PALETTE_COLOR_F64_XISF)

            for (format in formats) {
                val source0 = closeAfterEach(format.seekableSource())
                val hdus0 = XisfFormat.read(source0)

                hdus0 shouldHaveSize 1

                val outputPath = tempfile()
                val sink = closeAfterEach(outputPath.seekableSink())
                FitsFormat.write(sink, hdus0)

                val source1 = closeAfterEach(outputPath.seekableSource())
                val hdus1 = FitsFormat.read(source1).filterIsInstance<ImageHdu>()

                hdus1 shouldHaveSize 1
                hdus1[0].data.numberOfChannels shouldBeExactly 3
                // hdus1[0].header.size shouldBeExactly hdus0[0].header.size
                hdus1[0].data.red.size shouldBeExactly hdus0[0].data.red.size
                hdus1[0].data.red shouldNotBeSameInstanceAs hdus0[0].data.red
                hdus1[0].data.red.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.red[i] }
                hdus1[0].data.green shouldNotBeSameInstanceAs hdus0[0].data.green
                hdus1[0].data.green.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.green[i] }
                hdus1[0].data.blue shouldNotBeSameInstanceAs hdus0[0].data.blue
                hdus1[0].data.blue.forEachIndexed { i, value -> value shouldBeExactly hdus0[0].data.blue[i] }

                val bitpix = hdus1[0].header.bitpix
                val image = hdus1[0].makeImage()
                image.save("xisf-to-fits-color-$bitpix").second shouldBe "7233886f62065800b43419f3b1b6c833"
            }
        }
    }
}
