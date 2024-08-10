import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.fits.isFits
import nebulosa.image.Image.Companion.asImage
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.math.*
import nebulosa.pixinsight.script.*
import nebulosa.pixinsight.script.PixInsightScript.Companion.UNSPECIFIED_SLOT
import nebulosa.test.*
import nebulosa.xisf.isXisf
import nebulosa.xisf.xisf
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.math.roundToInt

@NonGitHubOnly
class PixInsightScriptTest : AbstractTest() {

    @Test
    @Disabled
    fun startup() {
        PixInsightStartup(PixInsightScript.DEFAULT_SLOT)
            .use { it.runSync(RUNNER).success.shouldBeTrue() }
    }

    @Test
    @Disabled
    fun isRunning() {
        PixInsightIsRunning(PixInsightScript.DEFAULT_SLOT)
            .use { it.runSync(RUNNER).success.shouldBeTrue() }
    }

    @Test
    fun calibrate() {
        val workingDirectory = tempDirectory("pi-")
        PixInsightCalibrate(UNSPECIFIED_SLOT, workingDirectory, PI_01_LIGHT, PI_DARK, PI_FLAT, PI_BIAS)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-calibrate").second shouldBe "731562ee12f45bf7c1095f4773f70e71"
    }

    @Test
    fun align() {
        val workingDirectory = tempDirectory("pi-")
        PixInsightAlign(UNSPECIFIED_SLOT, workingDirectory, PI_01_LIGHT, PI_02_LIGHT)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-align").second shouldBe "483ebaf15afa5957fe099f3ee2beff78"
    }

    @Test
    fun detectStars() {
        PixInsightDetectStars(UNSPECIFIED_SLOT, PI_FOCUS_0)
            .use { it.runSync(RUNNER).stars }
            .map { it.hfd }
            .average() shouldBe (8.43 plusOrMinus 1e-2)

        PixInsightDetectStars(UNSPECIFIED_SLOT, PI_FOCUS_30000)
            .use { it.runSync(RUNNER).stars }
            .map { it.hfd }
            .average() shouldBe (1.85 plusOrMinus 1e-2)

        PixInsightDetectStars(UNSPECIFIED_SLOT, PI_FOCUS_100000)
            .use { it.runSync(RUNNER).stars }
            .map { it.hfd }
            .average() shouldBe (18.35 plusOrMinus 1e-2)
    }

    @Test
    fun pixelMath() {
        val outputPath = tempPath("pi-stacked-", ".fits")
        PixInsightPixelMath(UNSPECIFIED_SLOT, listOf(PI_01_LIGHT, PI_02_LIGHT), outputPath, "{{0}} + {{1}}")
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-pixelmath").second shouldBe "cafc8138e2ce17614dcfa10edf410b07"
    }

    @Test
    fun abe() {
        val outputPath = tempPath("pi-abe-", ".fits")
        PixInsightAutomaticBackgroundExtractor(UNSPECIFIED_SLOT, PI_01_LIGHT, outputPath)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-abe").second shouldBe "bf62207dc17190009ba215da7c011297"
    }

    @Test
    fun lrgbCombination() {
        val outputPath = tempPath("pi-lrgb-", ".fits")
        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-lrgb").second shouldBe "99db35d78f7b360e7592217f4179b189"

        val weights = doubleArrayOf(1.0, 0.2470588, 0.31764705, 0.709803921) // LRGB #3F51B5
        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT, weights)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-weighted-lrgb").second shouldBe "1148ee222fbfb382ad2d708df5b0f79f"

        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, null, null)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-lr").second shouldBe "9100d3ce892f05f4b832b2fb5f35b5a1"

        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, null, PI_01_LIGHT, null)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-lg").second shouldBe "b4e8d8f7e289db60b41ba2bbe0035344"

        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, null, null, PI_01_LIGHT)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-lb").second shouldBe "1760e7cb1d139b63022dd975fe84897d"

        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, null, PI_01_LIGHT, PI_01_LIGHT, null)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-rg").second shouldBe "8c59307b5943932aefdf2dedfe1c8178"

        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, null, PI_01_LIGHT, null, PI_01_LIGHT)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-rb").second shouldBe "1bdf9cada6a33f76dceaccdaacf30fef"

        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, null, null, PI_01_LIGHT, PI_01_LIGHT)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-bg").second shouldBe "4a9c81c71fd37546fd300d1037742fa2"

        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT, null)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-lrg").second shouldBe "06c32c8679d409302423baa3a07fb241"

        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, null, PI_01_LIGHT)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-lrb").second shouldBe "f6d026cb63f7a58fc325e422c277ff89"

        PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, null, PI_01_LIGHT, PI_01_LIGHT)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().openAsImage() }
            .transform(AutoScreenTransformFunction).save("pi-lbg").second shouldBe "67f961110fb4b9f0033b3b8dbc8b1638"
    }

    @Test
    fun fileFormatConversion() {
        val xisfPath = tempPath("pi-ffc", ".xisf")
        PixInsightFileFormatConversion(UNSPECIFIED_SLOT, PI_01_LIGHT, xisfPath)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().isXisf().shouldBeTrue() }

        val fitsPath = tempPath("pi-ffc", ".fits")
        PixInsightFileFormatConversion(UNSPECIFIED_SLOT, xisfPath, fitsPath)
            .use { it.runSync(RUNNER).outputImage.shouldNotBeNull().isFits().shouldBeTrue() }
    }

    @Test
    fun imageSolver() {
        // https://nova.astrometry.net/user_images/10373761
        val path = download("https://nova.astrometry.net/image/14603", "jpg")
        val resolution = 6.78 // arcsec/px
        val centerRA = "06 40 51".hours
        val centerDEC = "09 49 53".deg

        // Estimated to match resolution = (pixelSize / focalDistance) * 206.265
        val focalDistance = 200.0 // mm
        val pixelSize = 6.58

        with(PixInsightImageSolver(UNSPECIFIED_SLOT, path, centerRA, centerDEC, pixelSize = pixelSize, resolution = resolution)
            .use { it.runSync(RUNNER) }) {
            success.shouldBeTrue()
            this.focalLength shouldBe (200.355 plusOrMinus 1e-5)
            this.pixelSize shouldBe (6.58 plusOrMinus 1e-2)
            this.resolution.toArcsec shouldBe (6.774 plusOrMinus 1e-3)
            rightAscension.formatHMS() shouldBe "06h40m51.8s"
            declination.formatSignedDMS() shouldBe "+009°49'53.6\""
            width.toArcmin shouldBe (90.321 plusOrMinus 1e-3)
            height.toArcmin shouldBe (59.386 plusOrMinus 1e-3)
            imageWidth.roundToInt() shouldBeExactly 800
            imageHeight.roundToInt() shouldBeExactly 526
        }

        with(PixInsightImageSolver(UNSPECIFIED_SLOT, path, centerRA, centerDEC, pixelSize = pixelSize, focalLength = focalDistance)
            .use { it.runSync(RUNNER) }) {
            success.shouldBeTrue()
            this.focalLength shouldBe (200.355 plusOrMinus 1e-5)
            this.pixelSize shouldBe (6.58 plusOrMinus 1e-2)
            this.resolution.toArcsec shouldBe (6.774 plusOrMinus 1e-3)
            rightAscension.formatHMS() shouldBe "06h40m51.8s"
            declination.formatSignedDMS() shouldBe "+009°49'53.6\""
            width.toArcmin shouldBe (90.321 plusOrMinus 1e-3)
            height.toArcmin shouldBe (59.386 plusOrMinus 1e-3)
            imageWidth.roundToInt() shouldBeExactly 800
            imageHeight.roundToInt() shouldBeExactly 526
        }
    }

    companion object {

        @JvmStatic val RUNNER = PixInsightScriptRunner(Path.of("PixInsight"))

        @JvmStatic
        internal fun Path.openAsImage() = if (isFits()) fits().asImage()
        else if (isXisf()) xisf().asImage()
        else throw IllegalArgumentException("the path at $this is not an image")
    }
}
