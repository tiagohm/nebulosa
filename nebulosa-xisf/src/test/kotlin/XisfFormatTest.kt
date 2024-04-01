import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import nebulosa.image.format.ImageHdu
import nebulosa.io.seekableSink
import nebulosa.io.seekableSource
import nebulosa.test.FitsStringSpec
import nebulosa.xisf.XisfFormat
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt

class XisfFormatTest : FitsStringSpec() {

    init {
        "mono:planar:8" {
            val source = closeAfterEach(M82_MONO_8_XISF.seekableSource())
            val hdus = XisfFormat.read(source)

            hdus shouldHaveSize 1

            with(hdus[0]) {
                width shouldBeExactly 512
                height shouldBeExactly 512
                numberOfChannels shouldBeExactly 1
                header shouldHaveSize 17
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
                header shouldHaveSize 17
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
                header shouldHaveSize 17
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
                header shouldHaveSize 17
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
                header shouldHaveSize 17
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
                header shouldHaveSize 17
                data.red.size shouldBeExactly 512 * 512
                data.green shouldNotBeSameInstanceAs data.red
                data.blue shouldNotBeSameInstanceAs data.green

                val image = makeImage()
                image.save("xisf-color-planar-8").second shouldBe "89beed384ee9e97ce033ba447a377937"
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
                header shouldHaveSize 17
                data.red.size shouldBeExactly 512 * 512
                data.green shouldNotBeSameInstanceAs data.red
                data.blue shouldNotBeSameInstanceAs data.green

                val image = makeImage()
                image.save("xisf-color-planar-16").second shouldBe "89beed384ee9e97ce033ba447a377937"
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
                header shouldHaveSize 17
                data.red.size shouldBeExactly 512 * 512
                data.green shouldNotBeSameInstanceAs data.red
                data.blue shouldNotBeSameInstanceAs data.green

                val image = makeImage()
                image.save("xisf-color-planar-32").second shouldBe "89beed384ee9e97ce033ba447a377937"
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
                header shouldHaveSize 17
                data.red.size shouldBeExactly 512 * 512
                data.green shouldNotBeSameInstanceAs data.red
                data.blue shouldNotBeSameInstanceAs data.green

                val image = makeImage()
                image.save("xisf-color-planar-F32").second shouldBe "89beed384ee9e97ce033ba447a377937"
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
                header shouldHaveSize 17
                data.red.size shouldBeExactly 512 * 512
                data.green shouldNotBeSameInstanceAs data.red
                data.blue shouldNotBeSameInstanceAs data.green

                val image = makeImage()
                image.save("xisf-color-planar-F64").second shouldBe "89beed384ee9e97ce033ba447a377937"
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
                header shouldHaveSize 17
                data.red.size shouldBeExactly 512 * 512
                data.green shouldBeSameInstanceAs data.red
                data.blue shouldBeSameInstanceAs data.green

                val image = makeImage()
                image.save("xisf-mono-planar-8-zlib").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
            }
        }
        "mono:write" {
            val source0 = closeAfterEach(M82_MONO_8_XISF.seekableSource())
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

            val image = hdus1[0].makeImage()
            image.save("xisf-mono-write").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
        }
        "color:write" {
            val source0 = closeAfterEach(M82_COLOR_32_XISF.seekableSource())
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

            val image = hdus1[0].makeImage()
            image.save("xisf-color-write").second shouldBe "89beed384ee9e97ce033ba447a377937"
        }
    }

    companion object {

        @JvmStatic
        private fun ImageHdu.makeImage(): BufferedImage {
            val type = if (numberOfChannels == 1) BufferedImage.TYPE_BYTE_GRAY else BufferedImage.TYPE_INT_RGB
            val image = BufferedImage(width, height, type)

            if (numberOfChannels == 1) {
                val buffer = (image.raster.dataBuffer as DataBufferByte)

                repeat(width * height) {
                    buffer.data[it] = (data.red[it] * 255f).toInt().toByte()
                }
            } else {
                val buffer = (image.raster.dataBuffer as DataBufferInt)

                repeat(width * height) {
                    val red = (data.red[it] * 255f).toInt() and 0xFF
                    val green = (data.green[it] * 255f).toInt() and 0xFF
                    val blue = (data.blue[it] * 255f).toInt() and 0xFF
                    buffer.data[it] = red or (green shl 8) or (blue shl 16)
                }
            }

            return image
        }
    }
}
