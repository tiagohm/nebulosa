import PixInsightScriptTest.Companion.RUNNER
import PixInsightScriptTest.Companion.openAsImage
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.pixinsight.livestacker.PixInsightLiveStacker
import nebulosa.test.AbstractTest
import nebulosa.test.NonGitHubOnly
import nebulosa.test.PI_01_LIGHT
import nebulosa.test.PI_02_LIGHT
import nebulosa.test.PI_03_LIGHT
import nebulosa.test.PI_04_LIGHT
import nebulosa.test.PI_05_LIGHT
import nebulosa.test.PI_06_LIGHT
import nebulosa.test.PI_07_LIGHT
import nebulosa.test.PI_08_LIGHT
import nebulosa.test.save
import org.junit.jupiter.api.Test
import java.nio.file.Path

@NonGitHubOnly
class PixInsightLiveStackerTest : AbstractTest() {

    @Test
    fun stack() {
        val files = listOf(PI_01_LIGHT, PI_02_LIGHT, PI_03_LIGHT, PI_04_LIGHT, PI_05_LIGHT, PI_06_LIGHT, PI_07_LIGHT, PI_08_LIGHT)
        val workingDirectory = tempDirectory("pi-")
        var outputPath: Path? = null

        val stacker = PixInsightLiveStacker(RUNNER, workingDirectory)

        try {
            stacker.start()

            for (file in files) {
                outputPath = stacker.add(file)
            }
        } finally {
            stacker.stop()
        }

        outputPath.shouldNotBeNull().openAsImage().transform(AutoScreenTransformFunction)
            .save("pi-live-stacked").second shouldBe "a107143dff3d43c4b56c872da869f89b"
    }
}
