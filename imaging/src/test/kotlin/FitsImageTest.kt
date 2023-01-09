import io.kotest.matchers.shouldBe
import nebulosa.imaging.FitsImage
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.*
import nom.tam.fits.Fits
import java.io.File
import java.util.*
import javax.imageio.ImageIO

@Suppress("BlockingMethodInNonBlockingContext")
class FitsImageTest : ImageTest() {

    init {
        "8-bits mono" {
            val fits = Fits("src/test/resources/M51.8.Mono.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.8.Mono.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "dda8e2886c0d31671953734cb7288cfc"
        }
        "16-bits mono" {
            val fits = Fits("src/test/resources/M51.16.Mono.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.16.Mono.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "876b787d9b86fd5de789f091610be65d"
        }
        "32-bits mono" {
            val fits = Fits("src/test/resources/M51.32.Mono.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.32.Mono.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "79640bc91bde46b473d6cac3f0d76e80"
        }
        "32-bits floating point mono" {
            val fits = Fits("src/test/resources/M51.F32.Mono.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.F32.Mono.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "79640bc91bde46b473d6cac3f0d76e80"
        }
        "64-bits floating point mono" {
            val fits = Fits("src/test/resources/M51.F64.Mono.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.F64.Mono.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "79640bc91bde46b473d6cac3f0d76e80"
        }
        "8-bits color" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.8.Color.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "5aa19e4d7aa87d3207f9c5f710698b2f"
        }
        "16-bits color" {
            val fits = Fits("src/test/resources/M51.16.Color.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.16.Color.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "74a31fd345d8af0764d304bab75eb021"
        }
        "32-bits color" {
            val fits = Fits("src/test/resources/M51.32.Color.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.32.Color.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "c4af1fe94f57529ff4aaefa7297f8acd"
        }
        "32-bits floating point color" {
            val fits = Fits("src/test/resources/M51.F32.Color.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.F32.Color.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "c4af1fe94f57529ff4aaefa7297f8acd"
        }
        "64-bits floating point color" {
            val fits = Fits("src/test/resources/M51.F64.Color.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.F64.Color.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "c4af1fe94f57529ff4aaefa7297f8acd"
        }
        "8-bits color full (ICC Profile + Properties + Thumbnail)" {
            val fits = Fits("src/test/resources/M51.8.Color.Full.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/M51.8.Color.Full.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "5aa19e4d7aa87d3207f9c5f710698b2f"
        }
        "STF midtone = 0.1, shadow = 0.0, highlight = 1.0" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.1f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.STF0.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "140be07d47a564b6a9fe3cc8a749ca8b"
        }
        "STF midtone = 0.9, shadow = 0.0, highlight = 1.0" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.9f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.STF1.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "86e456cbae3b0838c807db759075af22"
        }
        "STF midtone = 0.1, shadow = 0.5, highlight = 1.0" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.1f, shadow = 0.5f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.STF2.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "727cbe2ad73640efb077d6e4e70fa38e"
        }
        "STF midtone = 0.9, shadow = 0.5, highlight = 1.0" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.9f, shadow = 0.5f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.STF3.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "c3575a997ee68e3908b6623cc6f19aeb"
        }
        "STF midtone = 0.1, shadow = 0.0, highlight = 0.5" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.1f, highlight = 0.5f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.STF4.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "a86e45ad67b2c9c7106ca7b52f5e378c"
        }
        "STF midtone = 0.9, shadow = 0.0, highlight = 0.5" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.9f, highlight = 0.5f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.STF5.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "deabe6cf1fd64360c284d44b617e55f9"
        }
        "STF midtone = 0.1, shadow = 0.4, highlight = 0.6" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.1f, 0.4f, 0.6f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.STF6.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "a8632186ce0ea9d2ef545391e82e1f6c"
        }
        "STF midtone = 0.9, shadow = 0.4, highlight = 0.6" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = ScreenTransformFunction(0.9f, 0.4f, 0.6f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.STF7.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "c0872c612faaa601ec76191eaced7fcc"
        }
        "CCD Simulator - Stretch" {
            val fits = Fits("src/test/resources/CCD Simulator.Gray.fits")
            val image = ScreenTransformFunction(5.8e-5f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/CCD Simulator.Gray.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "27e35bc8cf0d946f4e121a87e4e3e751"
        }
        "CCD Simulator - JPG" {
            val fits = Fits("src/test/resources/CCD Simulator.Gray.fits")
            val image = ScreenTransformFunction(5.8e-5f).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/CCD Simulator.Gray.jpg")
            ImageIO.write(image, "JPG", outputFile)
            outputFile.md5() shouldBe "b8eaf66bb61d11ed1fab7f8273787616"
        }
        "Flip Vertical" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = Flip(vertical = true).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.FlipV.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "f28ab67afbe41fb2f07c7cbf76f1d1b1"
        }
        "Flip Horizontal" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = Flip(horizontal = true).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.FlipH.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "01da982b44c8a016ccfbe12c8ff12735"
        }
        "Flip Vertical & Horizontal" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = Flip(horizontal = true, vertical = true).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.FlipVH.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "9ae0f01a217478a07f3e67f834b353df"
        }
        "Invert" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = Invert.transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.Invert.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "376dfbbb2df0d936a1eed56ee36a5a3c"
        }
        "Grayscale" {
            val fits = Fits("src/test/resources/M51.8.Color.fits")
            val image = Grayscale.BT709.transform(FitsImage(fits))
            val outputFile = File("src/test/resources/M51.8.Color.Grayscale.BT709.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "a7313408fafa9c1d743ca34a481051b9"
        }
        "Debayer - GRBG" {
            val fits = Fits("src/test/resources/Debayer.GRBG.fits")
            val image = FitsImage(fits)
            val outputFile = File("src/test/resources/Debayer.GRBG.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "dad1a430e41e4846f0c6a9c594e5d57d"
        }
        "Salt & Pepper Noise" {
            val fits = Fits("src/test/resources/Flower.fits")
            val image = SaltAndPepperNoise(0.1f, Random(0)).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/Flower.SaltPepperNoise.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "7d15259b367ea973be204038f0972159"
        }
        "Blur" {
            val fits = Fits("src/test/resources/Flower.fits")
            val image = Blur().transform(FitsImage(fits))
            val outputFile = File("src/test/resources/Flower.Blur.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "ed3bda2192ea3298e33790715808de91"
        }
        "Gaussian Blur" {
            val fits = Fits("src/test/resources/Flower.fits")
            val image = GaussianBlur(sigma = 5.0, size = 9).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/Flower.GaussianBlur.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "bc43543e671a1201f71f5619a2e56463"
        }
        "Edges" {
            val fits = Fits("src/test/resources/Flower.fits")
            val image = Edges().transform(FitsImage(fits))
            val outputFile = File("src/test/resources/Flower.Edges.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "077818d1344d2b453ceed5caecbf657a"
        }
        "Sharpen" {
            val fits = Fits("src/test/resources/Flower.fits")
            val image = Sharpen().transform(FitsImage(fits))
            val outputFile = File("src/test/resources/Flower.Sharpen.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "bca1608df7b4bf9bb2d1b80d04c5fad0"
        }
        "Mean" {
            val fits = Fits("src/test/resources/Flower.fits")
            val image = TransformAlgorithm.of(SaltAndPepperNoise(0.1f, Random(0)), Mean()).transform(FitsImage(fits))
            val outputFile = File("src/test/resources/Flower.Mean.png")
            ImageIO.write(image, "PNG", outputFile)
            outputFile.md5() shouldBe "8a348a8393125ae35ad5478113c30c1e"
        }
    }
}
