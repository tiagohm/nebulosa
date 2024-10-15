import PixInsightScriptTest.Companion.RUNNER
import PixInsightScriptTest.Companion.openAsImage
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.pixinsight.stacker.PixInsightAutoStacker
import nebulosa.test.AbstractTest
import nebulosa.test.NonGitHubOnly
import nebulosa.test.fits.*
import nebulosa.test.save
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@NonGitHubOnly
class PixInsightAutoStackerTest : AbstractTest() {

    @Test
    fun stack() {
        val files = listOf(STACKING_LIGHT_MONO_01_FITS, STACKING_LIGHT_MONO_02_FITS, STACKING_LIGHT_MONO_03_FITS, STACKING_LIGHT_MONO_04_FITS, STACKING_LIGHT_MONO_05_FITS, STACKING_LIGHT_MONO_06_FITS, STACKING_LIGHT_MONO_07_FITS, STACKING_LIGHT_MONO_08_FITS)
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
        val files = listOf(STACKING_LIGHT_MONO_01_FITS, STACKING_LIGHT_MONO_02_FITS, STACKING_LIGHT_MONO_03_FITS, STACKING_LIGHT_MONO_04_FITS, STACKING_LIGHT_MONO_05_FITS, STACKING_LIGHT_MONO_06_FITS, STACKING_LIGHT_MONO_07_FITS, STACKING_LIGHT_MONO_08_FITS)
        val workingDirectory = tempDirectory("pi-")
        val outputPath = tempPath("pi-", ".fits")

        val stacker = PixInsightAutoStacker(RUNNER, workingDirectory, STACKING_DARK_MONO_FITS, STACKING_FLAT_MONO_FITS, STACKING_BIAS_MONO_FITS)
        stacker.stack(files, outputPath).shouldBeTrue()

        outputPath.openAsImage().transform(AutoScreenTransformFunction)
            .save("pi-calibrated-auto-stacked").second shouldBe ""
    }
}
