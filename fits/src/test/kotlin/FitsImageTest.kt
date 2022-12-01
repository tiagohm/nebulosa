import io.kotest.core.spec.style.StringSpec
import nebulosa.fits.FitsImage
import nebulosa.fits.algorithms.ScreenTransformFunction
import nom.tam.fits.Fits
import java.io.File
import javax.imageio.ImageIO

@Suppress("BlockingMethodInNonBlockingContext")
class FitsImageTest : StringSpec() {

    init {
        "8-bits mono" {
            val fits = Fits("src/test/resources/M51.8.Mono.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Mono.png"))
        }
        "16-bits mono" {
            val fits = Fits("src/test/resources/M51.16.Mono.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.16.Mono.png"))
        }
        "32-bits mono" {
            val fits = Fits("src/test/resources/M51.32.Mono.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.32.Mono.png"))
        }
        "32-bits floating point mono" {
            val fits = Fits("src/test/resources/M51.F32.Mono.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.F32.Mono.png"))
        }
        "64-bits floating point mono" {
            val fits = Fits("src/test/resources/M51.F64.Mono.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.F64.Mono.png"))
        }
        "8-bits color" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.png"))
        }
        "16-bits color" {
            val fits = Fits("src/test/resources/M51.16.Color.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.16.Color.png"))
        }
        "32-bits color" {
            val fits = Fits("src/test/resources/M51.32.Color.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.32.Color.png"))
        }
        "32-bits floating point color" {
            val fits = Fits("src/test/resources/M51.F32.Color.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.F32.Color.png"))
        }
        "64-bits floating point color" {
            val fits = Fits("src/test/resources/M51.F64.Color.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.F64.Color.png"))
        }
        "8-bits color full (ICC Profile + Properties + Thumbnail)" {
            val fits = Fits("src/test/resources/M51.8.Color.Full.fits")
            val image = FitsImage(fits)
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.Full.png"))
        }
        "STF midtone = 0.1, shadow = 0.0, highlight = 1.0" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.1f).transform(FitsImage(fits))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.STF0.png"))
        }
        "STF midtone = 0.9, shadow = 0.0, highlight = 1.0" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.9f).transform(FitsImage(fits))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.STF1.png"))
        }
        "STF midtone = 0.1, shadow = 0.5, highlight = 1.0" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.1f, shadow = 0.5f).transform(FitsImage(fits))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.STF2.png"))
        }
        "STF midtone = 0.9, shadow = 0.5, highlight = 1.0" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.9f, shadow = 0.5f).transform(FitsImage(fits))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.STF3.png"))
        }
        "STF midtone = 0.1, shadow = 0.0, highlight = 0.5" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.1f, highlight = 0.5f).transform(FitsImage(fits))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.STF4.png"))
        }
        "STF midtone = 0.9, shadow = 0.0, highlight = 0.5" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.9f, highlight = 0.5f).transform(FitsImage(fits))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.STF5.png"))
        }
        "STF midtone = 0.1, shadow = 0.4, highlight = 0.6" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.1f, 0.4f, 0.6f).transform(FitsImage(fits))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.STF6.png"))
        }
        "STF midtone = 0.9, shadow = 0.4, highlight = 0.6" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.9f, 0.4f, 0.6f).transform(FitsImage(fits))
            ImageIO.write(image, "PNG", File("src/test/resources/M51.8.Color.STF7.png"))
        }
        "CCD Simulator - Stretch" {
            val fits = Fits("src/test/resources/CCD Simulator.Gray.fits")
            val image = ScreenTransformFunction(5.8e-5f).transform(FitsImage(fits))
            ImageIO.write(image, "PNG", File("src/test/resources/CCD Simulator.Gray.png"))
        }
        "CCD Simulator - JPG" {
            val fits = Fits("src/test/resources/CCD Simulator.Gray.fits")
            val image = ScreenTransformFunction(5.8e-5f).transform(FitsImage(fits))
            ImageIO.write(image, "JPG", File("src/test/resources/CCD Simulator.Gray.jpg"))
        }
        "HorseHead" {
            val fits = Fits("src/test/resources/HorseHead.fits")
            val image = ScreenTransformFunction(0.74937f, 0.36090f, 0.79313f).transform(FitsImage(fits))
            ImageIO.write(image, "JPG", File("src/test/resources/HorseHead.jpg"))
        }
    }
}
