import PixInsightScriptTest.Companion.RUNNER
import PixInsightScriptTest.Companion.openAsImage
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.pixinsight.stacker.PixInsightStacker
import nebulosa.test.*
import org.junit.jupiter.api.Test

@NonGitHubOnly
class PixInsightStackerTest : AbstractTest() {

    @Test
    fun align() {
        val outputPath = tempPath("pi-", ".fits")
        val workingDirectory = tempDirectory("pi-")
        val stacker = PixInsightStacker(RUNNER, workingDirectory)
        stacker.align(PI_01_LIGHT, PI_03_LIGHT, outputPath).shouldBeTrue()

        outputPath.openAsImage().transform(AutoScreenTransformFunction)
            .save("pi-aligned").second shouldBe "106651a7c1e640852384284ec12e0977"
    }

    @Test
    fun calibrate() {
        val outputPath = tempPath("pi-", ".fits")
        val workingDirectory = tempDirectory("pi-")
        val stacker = PixInsightStacker(RUNNER, workingDirectory)
        stacker.calibrate(PI_01_LIGHT, outputPath, PI_DARK).shouldBeTrue()

        outputPath.openAsImage().transform(AutoScreenTransformFunction)
            .save("pi-calibrated").second shouldBe "8f5a2632c701680b41fcfe170c9cf468"
    }

    @Test
    fun integrate() {
        val outputPath = tempPath("pi-", ".fits")
        val workingDirectory = tempDirectory("pi-")
        val stacker = PixInsightStacker(RUNNER, workingDirectory)
        stacker.integrate(1, PI_01_LIGHT, PI_01_LIGHT, outputPath).shouldBeTrue()

        outputPath.openAsImage().transform(AutoScreenTransformFunction)
            .save("pi-integrated").second shouldBe "bf62207dc17190009ba215da7c011297"
    }

    @Test
    fun combineLrgb() {
        val outputPath = tempPath("pi-", ".fits")
        val workingDirectory = tempDirectory("pi-")
        val stacker = PixInsightStacker(RUNNER, workingDirectory)
        stacker.combineLRGB(outputPath, PI_01_LIGHT, PI_01_LIGHT).shouldBeTrue()

        outputPath.openAsImage().transform(AutoScreenTransformFunction)
            .save("pi-lrgb-combined").second shouldBe "9100d3ce892f05f4b832b2fb5f35b5a1"
    }

    @Test
    fun combineMonoLuminance() {
        val outputPath = tempPath("pi-", ".fits")
        val workingDirectory = tempDirectory("pi-")
        val stacker = PixInsightStacker(RUNNER, workingDirectory)
        stacker.combineLuminance(outputPath, PI_01_LIGHT, PI_01_LIGHT, true).shouldBeTrue()

        outputPath.openAsImage().transform(AutoScreenTransformFunction)
            .save("pi-mono-luminance-combined").second shouldBe "85de365a9895234222acdc6e9feb7009"
    }
}
