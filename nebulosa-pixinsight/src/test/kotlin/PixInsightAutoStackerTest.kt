import PixInsightScriptTest.Companion.openAsImage
import io.kotest.core.annotation.EnabledIf
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.stacker.PixInsightAutoStacker
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path

@EnabledIf(NonGitHubOnlyCondition::class)
class PixInsightAutoStackerTest : AbstractFitsAndXisfTest() {

    init {
        val runner = PixInsightScriptRunner(Path.of("PixInsight"))
        val workingDirectory = tempdir("pi-").toPath()

        "stack" {
            val files = listOf(PI_01_LIGHT, PI_02_LIGHT, PI_03_LIGHT, PI_04_LIGHT, PI_05_LIGHT, PI_06_LIGHT, PI_07_LIGHT, PI_08_LIGHT)
            val outputPath = tempfile("pi-", ".fits").toPath()

            val stacker = PixInsightAutoStacker(runner, workingDirectory)
            stacker.stack(files, outputPath).shouldBeTrue()

            outputPath.openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-auto-stacked").second shouldBe "a107143dff3d43c4b56c872da869f89b"
        }
        "!calibrated stack" {
            val files = listOf(PI_01_LIGHT, PI_02_LIGHT, PI_03_LIGHT, PI_04_LIGHT, PI_05_LIGHT, PI_06_LIGHT, PI_07_LIGHT, PI_08_LIGHT)
            val outputPath = tempfile("pi-", ".fits").toPath()

            val stacker = PixInsightAutoStacker(runner, workingDirectory, PI_DARK, PI_FLAT, PI_BIAS)
            stacker.stack(files, outputPath).shouldBeTrue()

            outputPath.openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-calibrated-auto-stacked").second shouldBe ""
        }
    }
}
