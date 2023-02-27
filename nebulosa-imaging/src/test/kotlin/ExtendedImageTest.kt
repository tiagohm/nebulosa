import io.kotest.core.annotation.Ignored
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.imaging.Image
import nom.tam.fits.Fits
import java.io.File
import javax.imageio.ImageIO

@Ignored
@Suppress("BlockingMethodInNonBlockingContext")
class ExtendedImageTest : ImageTest() {

    init {
        beforeSpec {
            var fits = Fits("src/test/resources/M51.8.Mono.fits")
            var image = Image.open(fits)
            ImageIO.write(image, "JPEG", File("src/test/resources/M51.8.Mono.Extended.jpg"))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Mono.Extended.png"))
            ImageIO.write(image, "BMP", File("src/test/resources/M51.8.Mono.Extended.bmp"))

            fits = Fits("src/test/resources/M51.8.Color.fits")
            image = Image.open(fits)
            ImageIO.write(image, "JPEG", File("src/test/resources/M51.8.Color.Extended.jpg"))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.Extended.png"))
            ImageIO.write(image, "BMP", File("src/test/resources/M51.8.Color.Extended.bmp"))
        }

        "jpeg mono" {
            val inputFile = File("src/test/resources/M51.8.Mono.Extended.jpg")
            val image = Image.open(inputFile)
            val outputFile = File("src/test/resources/M51.8.Mono.Extended-V2.jpg")
            ImageIO.write(image, "JPEG", outputFile)

            compareSomePixels(inputFile, outputFile)
        }
        "jpeg color" {
            val inputFile = File("src/test/resources/M51.8.Color.Extended.jpg")
            val image = Image.open(inputFile)
            val outputFile = File("src/test/resources/M51.8.Color.Extended-V2.jpg")
            ImageIO.write(image, "JPEG", outputFile)

            compareSomePixels(inputFile, outputFile)
        }
        "png mono" {
            val inputFile = File("src/test/resources/M51.8.Mono.Extended.png")
            val image = Image.open(inputFile)
            val outputFile = File("src/test/resources/M51.8.Mono.Extended-V2.png")
            ImageIO.write(image, "JPEG", outputFile)

            compareSomePixels(inputFile, outputFile)
        }
        "png color" {
            val inputFile = File("src/test/resources/M51.8.Color.Extended.png")
            val image = Image.open(inputFile)
            val outputFile = File("src/test/resources/M51.8.Color.Extended-V2.png")
            ImageIO.write(image, "JPEG", outputFile)

            compareSomePixels(inputFile, outputFile)
        }
        "bmp mono" {
            val inputFile = File("src/test/resources/M51.8.Mono.Extended.bmp")
            val image = Image.open(inputFile)
            val outputFile = File("src/test/resources/M51.8.Mono.Extended-V2.bmp")
            ImageIO.write(image, "JPEG", outputFile)

            compareSomePixels(inputFile, outputFile)
        }
        "bmp color" {
            val inputFile = File("src/test/resources/M51.8.Color.Extended.bmp")
            val image = Image.open(inputFile)
            val outputFile = File("src/test/resources/M51.8.Color.Extended-V2.bmp")
            ImageIO.write(image, "JPEG", outputFile)

            compareSomePixels(inputFile, outputFile)
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
