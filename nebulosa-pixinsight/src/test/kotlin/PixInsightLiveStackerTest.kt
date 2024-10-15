import PixInsightScriptTest.Companion.RUNNER
import PixInsightScriptTest.Companion.openAsImage
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.pixinsight.livestacker.PixInsightLiveStacker
import nebulosa.test.AbstractTest
import nebulosa.test.NonGitHubOnly
import nebulosa.test.fits.*
import nebulosa.test.save
import org.junit.jupiter.api.Test
import java.nio.file.Path

@NonGitHubOnly
class PixInsightLiveStackerTest : AbstractTest() {

    private val files = listOf(STACKING_LIGHT_MONO_01_FITS, STACKING_LIGHT_MONO_02_FITS, STACKING_LIGHT_MONO_03_FITS, STACKING_LIGHT_MONO_04_FITS, STACKING_LIGHT_MONO_05_FITS, STACKING_LIGHT_MONO_06_FITS, STACKING_LIGHT_MONO_07_FITS, STACKING_LIGHT_MONO_08_FITS)

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
                outputPath = stacker.add(file, STACKING_LIGHT_MONO_04_FITS)
            }

            outputPath.shouldNotBeNull().openAsImage().transform(AutoScreenTransformFunction)
                .save("pi-live-stacked-with-reference").second shouldBe "1345e78b085368e3d3a1b5dc9e00f3ba"
        }

        stacker.isRunning.shouldBeFalse()
    }
}
