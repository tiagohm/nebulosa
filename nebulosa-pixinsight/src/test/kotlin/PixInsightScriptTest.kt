import io.kotest.core.annotation.EnabledIf
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.fits.isFits
import nebulosa.image.Image
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.math.*
import nebulosa.pixinsight.script.*
import nebulosa.pixinsight.script.PixInsightScript.Companion.UNSPECIFIED_SLOT
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import nebulosa.xisf.isXisf
import nebulosa.xisf.xisf
import java.nio.file.Path
import kotlin.math.roundToInt

@EnabledIf(NonGitHubOnlyCondition::class)
class PixInsightScriptTest : AbstractFitsAndXisfTest() {

    init {
        val runner = PixInsightScriptRunner(Path.of("PixInsight"))
        val workingDirectory = tempdir("pi-").toPath()

        "!startup" {
            PixInsightStartup(PixInsightScript.DEFAULT_SLOT)
                .use { it.runSync(runner).success.shouldBeTrue() }
        }
        "!is running" {
            PixInsightIsRunning(PixInsightScript.DEFAULT_SLOT)
                .use { it.runSync(runner).success.shouldBeTrue() }
        }
        "calibrate" {
            PixInsightCalibrate(UNSPECIFIED_SLOT, workingDirectory, PI_01_LIGHT, PI_DARK, PI_FLAT, PI_BIAS)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-calibrate").second shouldBe "731562ee12f45bf7c1095f4773f70e71"
        }
        "align" {
            PixInsightAlign(UNSPECIFIED_SLOT, workingDirectory, PI_01_LIGHT, PI_02_LIGHT)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-align").second shouldBe "483ebaf15afa5957fe099f3ee2beff78"
        }
        "detect stars" {
            PixInsightDetectStars(UNSPECIFIED_SLOT, PI_FOCUS_0)
                .use { it.runSync(runner).stars }
                .map { it.hfd }
                .average() shouldBe (8.43 plusOrMinus 1e-2)

            PixInsightDetectStars(UNSPECIFIED_SLOT, PI_FOCUS_30000)
                .use { it.runSync(runner).stars }
                .map { it.hfd }
                .average() shouldBe (1.85 plusOrMinus 1e-2)

            PixInsightDetectStars(UNSPECIFIED_SLOT, PI_FOCUS_100000)
                .use { it.runSync(runner).stars }
                .map { it.hfd }
                .average() shouldBe (18.35 plusOrMinus 1e-2)
        }
        "pixel math" {
            val outputPath = tempfile("pi-stacked-", ".fits").toPath()
            PixInsightPixelMath(UNSPECIFIED_SLOT, listOf(PI_01_LIGHT, PI_02_LIGHT), outputPath, "{{0}} + {{1}}")
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-pixelmath").second shouldBe "cafc8138e2ce17614dcfa10edf410b07"
        }
        "abe" {
            val outputPath = tempfile("pi-abe-", ".fits").toPath()
            PixInsightAutomaticBackgroundExtractor(UNSPECIFIED_SLOT, PI_01_LIGHT, outputPath)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-abe").second shouldBe "bf62207dc17190009ba215da7c011297"
        }
        "lrgb combination" {
            val outputPath = tempfile("pi-lrgb-", ".fits").toPath()
            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-lrgb").second shouldBe "99db35d78f7b360e7592217f4179b189"

            val weights = doubleArrayOf(1.0, 0.2470588, 0.31764705, 0.709803921) // LRGB #3F51B5
            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT, weights)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-weighted-lrgb").second shouldBe "1148ee222fbfb382ad2d708df5b0f79f"

            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, null, null)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-lr").second shouldBe "9100d3ce892f05f4b832b2fb5f35b5a1"

            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, null, PI_01_LIGHT, null)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-lg").second shouldBe "b4e8d8f7e289db60b41ba2bbe0035344"

            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, null, null, PI_01_LIGHT)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-lb").second shouldBe "1760e7cb1d139b63022dd975fe84897d"

            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, null, PI_01_LIGHT, PI_01_LIGHT, null)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-rg").second shouldBe "8c59307b5943932aefdf2dedfe1c8178"

            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, null, PI_01_LIGHT, null, PI_01_LIGHT)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-rb").second shouldBe "1bdf9cada6a33f76dceaccdaacf30fef"

            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, null, null, PI_01_LIGHT, PI_01_LIGHT)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-bg").second shouldBe "4a9c81c71fd37546fd300d1037742fa2"

            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT, null)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-lrg").second shouldBe "06c32c8679d409302423baa3a07fb241"

            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, null, PI_01_LIGHT)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-lrb").second shouldBe "f6d026cb63f7a58fc325e422c277ff89"

            PixInsightLRGBCombination(UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, null, PI_01_LIGHT, PI_01_LIGHT)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-lbg").second shouldBe "67f961110fb4b9f0033b3b8dbc8b1638"
        }
        "file format conversion" {
            val xisfPath = tempfile("pi-ffc", ".xisf").toPath()
            PixInsightFileFormatConversion(UNSPECIFIED_SLOT, PI_01_LIGHT, xisfPath)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().isXisf().shouldBeTrue() }

            val fitsPath = tempfile("pi-ffc", ".fits").toPath()
            PixInsightFileFormatConversion(UNSPECIFIED_SLOT, xisfPath, fitsPath)
                .use { it.runSync(runner).outputImage.shouldNotBeNull().isFits().shouldBeTrue() }
        }
        "image solver" {
            // https://nova.astrometry.net/user_images/10373761
            val path = download("https://nova.astrometry.net/image/14603", "jpg")
            val resolution = 6.78 // arcsec/px
            val centerRA = "06 40 51".hours
            val centerDEC = "09 49 53".deg

            // Estimated to match resolution = (pixelSize / focalDistance) * 206.265
            val focalDistance = 200.0 // mm
            val pixelSize = 6.58

            with(PixInsightImageSolver(UNSPECIFIED_SLOT, path, centerRA, centerDEC, pixelSize = pixelSize, resolution = resolution)
                .use { it.runSync(runner) }) {
                success.shouldBeTrue()
                this.focalDistance shouldBe (200.355 plusOrMinus 1e-5)
                this.pixelSize shouldBe (6.58 plusOrMinus 1e-2)
                this.resolution.toArcsec shouldBe (6.774 plusOrMinus 1e-3)
                rightAscension.formatHMS() shouldBe "06h40m51.8s"
                declination.formatSignedDMS() shouldBe "+009°49'53.6\""
                width.toArcmin shouldBe (90.321 plusOrMinus 1e-3)
                height.toArcmin shouldBe (59.386 plusOrMinus 1e-3)
                imageWidth.roundToInt() shouldBeExactly 800
                imageHeight.roundToInt() shouldBeExactly 526
            }

            with(PixInsightImageSolver(UNSPECIFIED_SLOT, path, centerRA, centerDEC, pixelSize = pixelSize, focalDistance = focalDistance)
                .use { it.runSync(runner) }) {
                success.shouldBeTrue()
                this.focalDistance shouldBe (200.355 plusOrMinus 1e-5)
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
    }

    companion object {

        @JvmStatic
        internal fun Path.openAsImage(): Image {
            return if (isFits()) fits().use(Image::open)
            else if (isXisf()) xisf().use(Image::open)
            else throw IllegalArgumentException("the path at $this is not an image")
        }
    }
}
