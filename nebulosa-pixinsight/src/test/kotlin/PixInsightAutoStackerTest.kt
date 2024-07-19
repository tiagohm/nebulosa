import PixInsightScriptTest.Companion.openAsImage
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.pixinsight.script.PixInsightScriptRunner
import nebulosa.pixinsight.stacker.PixInsightAutoStacker
import nebulosa.test.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Path

@NonGitHubOnly
class PixInsightAutoStackerTest : AbstractTest() {

    @Test
    fun stack() {
        val files = listOf(PI_01_LIGHT, PI_02_LIGHT, PI_03_LIGHT, PI_04_LIGHT, PI_05_LIGHT, PI_06_LIGHT, PI_07_LIGHT, PI_08_LIGHT)
        val workingDirectory = tempDirectory("pi-")
        val outputPath = tempPath("pi-", ".fits")

        val stacker = PixInsightAutoStacker(RUNNER, workingDirectory)
        stacker.stack(files, outputPath).shouldBeTrue()

        outputPath.openAsImage().transform(AutoScreenTransformFunction)
            .save("pi-auto-stacked").second shouldBe "a107143dff3d43c4b56c872da869f89b"
    }

    @Test
    @Disabled
    fun calibratedStack() {
        val files = listOf(PI_01_LIGHT, PI_02_LIGHT, PI_03_LIGHT, PI_04_LIGHT, PI_05_LIGHT, PI_06_LIGHT, PI_07_LIGHT, PI_08_LIGHT)
        val workingDirectory = tempDirectory("pi-")
        val outputPath = tempPath("pi-", ".fits")

        val stacker = PixInsightAutoStacker(RUNNER, workingDirectory, PI_DARK, PI_FLAT, PI_BIAS)
        stacker.stack(files, outputPath).shouldBeTrue()

        outputPath.openAsImage().transform(AutoScreenTransformFunction)
            .save("pi-calibrated-auto-stacked").second shouldBe ""
    }

    companion object {

        @JvmStatic private val RUNNER = PixInsightScriptRunner(Path.of("PixInsight"))
    }
}
