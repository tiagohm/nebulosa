import io.kotest.core.annotation.EnabledIf
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import nebulosa.pixinsight.script.*
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Files
import java.nio.file.Path

@EnabledIf(NonGitHubOnlyCondition::class)
class PixInsightScriptTest : AbstractFitsAndXisfTest() {

    init {
        val runner = PixInsightScriptRunner(Path.of("PixInsight"))
        val workingDirectory = tempdir("pi-").toPath()

        "startup" {
            PixInsightStartup(PixInsightScript.DEFAULT_SLOT)
                .use { it.runSync(runner).shouldBeTrue() }
        }
        "is running" {
            PixInsightIsRunning(PixInsightScript.DEFAULT_SLOT)
                .use { it.runSync(runner).shouldBeTrue() }
        }
        "calibrate" {
            PixInsightCalibrate(PixInsightScript.UNSPECIFIED_SLOT, workingDirectory, PI_01_LIGHT, PI_DARK, PI_FLAT, PI_BIAS)
                .use { it.runSync(runner).also(::println).outputImage.shouldNotBeNull().shouldExist() }
        }
        "align" {
            PixInsightAlign(PixInsightScript.UNSPECIFIED_SLOT, workingDirectory, PI_01_LIGHT, PI_02_LIGHT)
                .use { it.runSync(runner).also(::println).outputImage.shouldNotBeNull().shouldExist() }
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
            val outputPath = Files.createTempFile("pi-stacked-", ".fits")
            PixInsightPixelMath(PixInsightScript.UNSPECIFIED_SLOT, listOf(PI_01_LIGHT, PI_02_LIGHT), outputPath, "{{0}} + {{1}}")
                .use { it.runSync(runner).also(::println).stackedImage.shouldNotBeNull().shouldExist() }
        }
        "abe" {
            val outputPath = tempfile("pi-", ".fits").toPath()
            PixInsightAutomaticBackgroundExtractor(PixInsightScript.UNSPECIFIED_SLOT, PI_01_LIGHT, outputPath)
                .use { it.runSync(runner).also(::println).outputImage.shouldNotBeNull() }
        }
    }
}
