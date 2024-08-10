import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.alpaca.api.AlpacaService
import nebulosa.test.NonGitHubOnly
import org.junit.jupiter.api.Test

@NonGitHubOnly
class AlpacaServiceTest {

    @Test
    fun management() {
        val body = CLIENT.management.configuredDevices().execute().body().shouldNotBeNull()

        for (device in body.value) {
            println(device)
        }
    }

    companion object {

        @JvmStatic val CLIENT = AlpacaService("http://localhost:11111/")
    }
}
