import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import nebulosa.pixinsight.PixInsightIsRunning
import nebulosa.pixinsight.PixInsightScriptRunner
import nebulosa.pixinsight.PixInsightStartup
import nebulosa.test.NonGitHubOnlyCondition
import java.nio.file.Path

@EnabledIf(NonGitHubOnlyCondition::class)
class PixInsightScriptTest : StringSpec() {

    init {
        val runner = PixInsightScriptRunner(Path.of("PixInsight"))

        "startup" {
            PixInsightStartup().runSync(runner).shouldBeTrue()
        }
        "is running" {
            PixInsightIsRunning().runSync(runner).shouldBeTrue()
        }
    }
}
