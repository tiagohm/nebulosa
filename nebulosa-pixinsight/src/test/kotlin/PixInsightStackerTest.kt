import PixInsightScriptTest.Companion.openAsImage
import io.kotest.core.annotation.EnabledIf
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.stacker.PixInsightStacker
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path

@EnabledIf(NonGitHubOnlyCondition::class)
class PixInsightStackerTest : AbstractFitsAndXisfTest() {

    init {
        val runner = PixInsightScriptRunner(Path.of("PixInsight"))
        val workingDirectory = tempdir("pi-").toPath()
        val stacker = PixInsightStacker(runner, workingDirectory)

        "align" {
            val outputPath = tempfile("pi-", ".fits").toPath()
            stacker.align(PI_01_LIGHT, PI_03_LIGHT, outputPath).shouldBeTrue()

            outputPath.openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-aligned").second shouldBe "106651a7c1e640852384284ec12e0977"
        }
        "calibrate" {
            val outputPath = tempfile("pi-", ".fits").toPath()
            stacker.calibrate(PI_01_LIGHT, outputPath, PI_DARK).shouldBeTrue()

            outputPath.openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-calibrated").second shouldBe "8f5a2632c701680b41fcfe170c9cf468"
        }
        "integrate" {
            val outputPath = tempfile("pi-", ".fits").toPath()
            stacker.integrate(1, PI_01_LIGHT, PI_01_LIGHT, outputPath).shouldBeTrue()

            outputPath.openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-integrated").second shouldBe "bf62207dc17190009ba215da7c011297"
        }
        "combine LRGB" {
            val outputPath = tempfile("pi-", ".fits").toPath()
            stacker.combineLRGB(outputPath, PI_01_LIGHT, PI_01_LIGHT).shouldBeTrue()

            outputPath.openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-lrgb-combined").second shouldBe "9100d3ce892f05f4b832b2fb5f35b5a1"
        }
        "combine mono luminance" {
            val outputPath = tempfile("pi-", ".fits").toPath()
            stacker.combineLuminance(outputPath, PI_01_LIGHT, PI_01_LIGHT, true).shouldBeTrue()

            outputPath.openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-mono-luminance-combined").second shouldBe "85de365a9895234222acdc6e9feb7009"
        }
    }
}
