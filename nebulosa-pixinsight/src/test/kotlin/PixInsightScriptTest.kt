import io.kotest.core.annotation.EnabledIf
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.fits.isFits
import nebulosa.image.Image
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.pixinsight.script.*
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import nebulosa.xisf.isXisf
import nebulosa.xisf.xisf
import java.nio.file.Path

@EnabledIf(NonGitHubOnlyCondition::class)
class PixInsightScriptTest : AbstractFitsAndXisfTest() {

    init {
        val runner = PixInsightScriptRunner(Path.of("PixInsight"))
        val workingDirectory = tempdir("pi-").toPath()

        "!startup" {
            PixInsightStartup(PixInsightScript.DEFAULT_SLOT)
                .use { it.runSync(runner).shouldBeTrue() }
        }
        "!is running" {
            PixInsightIsRunning(PixInsightScript.DEFAULT_SLOT)
                .use { it.runSync(runner).shouldBeTrue() }
        }
        "calibrate" {
            PixInsightCalibrate(PixInsightScript.UNSPECIFIED_SLOT, workingDirectory, PI_01_LIGHT, PI_DARK, PI_FLAT, PI_BIAS)
                .use { it.runSync(runner).also(::println).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-calibrate").second shouldBe "731562ee12f45bf7c1095f4773f70e71"
        }
        "align" {
            PixInsightAlign(PixInsightScript.UNSPECIFIED_SLOT, workingDirectory, PI_01_LIGHT, PI_02_LIGHT)
                .use { it.runSync(runner).also(::println).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-align").second shouldBe "483ebaf15afa5957fe099f3ee2beff78"
        }
        "detect stars" {
            PixInsightDetectStars(PixInsightScript.UNSPECIFIED_SLOT, PI_FOCUS_0)
                .use { it.runSync(runner).also(::println).stars }
                .map { it.hfd }
                .average() shouldBe (8.43 plusOrMinus 1e-2)

            PixInsightDetectStars(PixInsightScript.UNSPECIFIED_SLOT, PI_FOCUS_30000)
                .use { it.runSync(runner).also(::println).stars }
                .map { it.hfd }
                .average() shouldBe (1.85 plusOrMinus 1e-2)

            PixInsightDetectStars(PixInsightScript.UNSPECIFIED_SLOT, PI_FOCUS_100000)
                .use { it.runSync(runner).also(::println).stars }
                .map { it.hfd }
                .average() shouldBe (18.35 plusOrMinus 1e-2)
        }
        "pixel math" {
            val outputPath = tempfile("pi-stacked-", ".fits").toPath()
            PixInsightPixelMath(PixInsightScript.UNSPECIFIED_SLOT, listOf(PI_01_LIGHT, PI_02_LIGHT), outputPath, "{{0}} + {{1}}")
                .use { it.runSync(runner).also(::println).stackedImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-pixelmath").second shouldBe "cafc8138e2ce17614dcfa10edf410b07"
        }
        "abe" {
            val outputPath = tempfile("pi-abe-", ".fits").toPath()
            PixInsightAutomaticBackgroundExtractor(PixInsightScript.UNSPECIFIED_SLOT, PI_01_LIGHT, outputPath)
                .use { it.runSync(runner).also(::println).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-abe").second shouldBe "bf62207dc17190009ba215da7c011297"
        }
        "lrgb combination" {
            val outputPath = tempfile("pi-lrgb-", ".fits").toPath()
            PixInsightLRGBCombination(PixInsightScript.UNSPECIFIED_SLOT, outputPath, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT, PI_01_LIGHT)
                .use { it.runSync(runner).also(::println).outputImage.shouldNotBeNull().openAsImage() }
                .transform(AutoScreenTransformFunction).save("pi-lrgb").second shouldBe "99db35d78f7b360e7592217f4179b189"
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
