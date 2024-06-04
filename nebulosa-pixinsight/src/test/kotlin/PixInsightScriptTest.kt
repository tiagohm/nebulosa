import io.kotest.core.annotation.EnabledIf
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.paths.shouldExist
import nebulosa.pixinsight.script.*
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path

@EnabledIf(NonGitHubOnlyCondition::class)
class PixInsightScriptTest : AbstractFitsAndXisfTest() {

    init {
        val runner = PixInsightScriptRunner(Path.of("PixInsight"))

        "startup" {
            PixInsightStartup(PixInsightScript.DEFAULT_SLOT)
                .use { it.runSync(runner).shouldBeTrue() }
        }
        "is running" {
            PixInsightIsRunning(PixInsightScript.DEFAULT_SLOT)
                .use { it.runSync(runner).shouldBeTrue() }
        }
        "calibrate" {
            PixInsightCalibrate(PixInsightScript.DEFAULT_SLOT, PI_01_LIGHT, PI_DARK, PI_FLAT, PI_BIAS)
                .use { it.runSync(runner).shouldNotBeNull().shouldExist() }
        }
    }
}
