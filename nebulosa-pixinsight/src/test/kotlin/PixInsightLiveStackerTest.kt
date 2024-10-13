import PixInsightScriptTest.Companion.RUNNER
import PixInsightScriptTest.Companion.openAsImage
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.pixinsight.livestacker.PixInsightLiveStacker
import nebulosa.test.*
import org.junit.jupiter.api.Test
import java.nio.file.Path

@NonGitHubOnly
class PixInsightLiveStackerTest : AbstractTest() {

    private val files = listOf(PI_01_LIGHT, PI_02_LIGHT, PI_03_LIGHT, PI_04_LIGHT, PI_05_LIGHT, PI_06_LIGHT, PI_07_LIGHT, PI_08_LIGHT)

    @Test
    fun stack() {
        val workingDirectory = tempDirectory("pi-")
        val stacker = PixInsightLiveStacker(RUNNER, workingDirectory)
        var outputPath: Path? = null

        stacker.use {
            it.start()

            it.isRunning.shouldBeTrue()

            for (file in files) {
                outputPath = it.add(file)
            }

            outputPath.shouldNotBeNull().openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-live-stacked").second shouldBe "a107143dff3d43c4b56c872da869f89b"
        }

        stacker.isRunning.shouldBeFalse()
    }

    @Test
    fun stackWithReference() {
        val workingDirectory = tempDirectory("pi-")
        val stacker = PixInsightLiveStacker(RUNNER, workingDirectory)
        var outputPath: Path? = null

        stacker.use {
            stacker.start()

            it.isRunning.shouldBeTrue()

            for (file in files) {
                outputPath = stacker.add(file, PI_04_LIGHT)
            }

            outputPath.shouldNotBeNull().openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-live-stacked-with-reference").second shouldBe "1345e78b085368e3d3a1b5dc9e00f3ba"
        }

        stacker.isRunning.shouldBeFalse()
    }
}
