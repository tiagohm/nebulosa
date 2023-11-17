import io.kotest.core.annotation.Ignored
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.fits.Fits
import nebulosa.imaging.Image
import java.io.File
import javax.imageio.ImageIO

@Ignored
@Suppress("BlockingMethodInNonBlockingContext")
class ExtendedImageTest : AbstractImageTest() {

    init {
        beforeSpec {
            var fits = Fits("src/test/resources/M51.8.Mono.fits")
            fits.read()
            var image = Image.openFITS(fits)
            ImageIO.write(image, "JPEG", File("src/test/resources/M51.8.Mono.Extended.jpg"))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Mono.Extended.png"))
            ImageIO.write(image, "BMP", File("src/test/resources/M51.8.Mono.Extended.bmp"))

            fits = Fits("src/test/resources/M51.8.Color.fits")
            fits.read()
            image = Image.openFITS(fits)
            ImageIO.write(image, "JPEG", File("src/test/resources/M51.8.Color.Extended.jpg"))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.Extended.png"))
            ImageIO.write(image, "BMP", File("src/test/resources/M51.8.Color.Extended.bmp"))
        }

        "jpeg mono" {
            val input = File("src/test/resources/M51.8.Mono.Extended.jpg")
            val image = Image.openImage(ImageIO.read(input))
            val output = File("src/test/resources/M51.8.Mono.Extended-V2.jpg")
            ImageIO.write(image, "JPEG", output)

            compareSomePixels(input, output)
        }
        "jpeg color" {
            val input = File("src/test/resources/M51.8.Color.Extended.jpg")
            val image = Image.openImage(ImageIO.read(input))
            val output = File("src/test/resources/M51.8.Color.Extended-V2.jpg")
            ImageIO.write(image, "JPEG", output)

            compareSomePixels(input, output)
        }
        "png mono" {
            val input = File("src/test/resources/M51.8.Mono.Extended.png")
            val image = Image.openImage(ImageIO.read(input))
            val output = File("src/test/resources/M51.8.Mono.Extended-V2.png")
            ImageIO.write(image, "JPEG", output)

            compareSomePixels(input, output)
        }
        "png color" {
            val input = File("src/test/resources/M51.8.Color.Extended.png")
            val image = Image.openImage(ImageIO.read(input))
            val output = File("src/test/resources/M51.8.Color.Extended-V2.png")
            ImageIO.write(image, "JPEG", output)

            compareSomePixels(input, output)
        }
        "bmp mono" {
            val input = File("src/test/resources/M51.8.Mono.Extended.bmp")
            val image = Image.openImage(ImageIO.read(input))
            val output = File("src/test/resources/M51.8.Mono.Extended-V2.bmp")
            ImageIO.write(image, "JPEG", output)

            compareSomePixels(input, output)
        }
        "bmp color" {
            val input = File("src/test/resources/M51.8.Color.Extended.bmp")
            val image = Image.openImage(ImageIO.read(input))
            val output = File("src/test/resources/M51.8.Color.Extended-V2.bmp")
            ImageIO.write(image, "JPEG", output)

            compareSomePixels(input, output)
        }
    }

    private fun compareSomePixels(a: File, b: File) {
        val c = ImageIO.read(a)
        val d = ImageIO.read(b)

        for (i in 0..99) {
            val x = (Math.random() * c.width).toInt()
            val y = (Math.random() * c.height).toInt()
            c.getRGB(x, y) shouldBeExactly d.getRGB(x, y)
        }
    }
}
