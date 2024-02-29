import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.alpaca.api.AlpacaService
import nebulosa.test.NonGitHubOnlyCondition

@EnabledIf(NonGitHubOnlyCondition::class)
class AlpacaServiceTest : StringSpec() {

    init {
        val client = AlpacaService("http://localhost:11111/")

        "management" {
            val body = client.management.configuredDevices().execute().body().shouldNotBeNull()

            for (device in body.value) {
                println(device)
            }
        }
    }
}
