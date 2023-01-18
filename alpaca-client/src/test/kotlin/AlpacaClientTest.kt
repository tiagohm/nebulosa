import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import nebulosa.alpaca.client.AlpacaClient

class AlpacaClientTest : StringSpec() {

    init {
        val client = AlpacaClient("https://virtserver.swaggerhub.com/ASCOMInitiative/api/v1/")

        "camera" {
            client.camera.isConnected(0).value!!.shouldBeTrue()
            client.camera.connect(0, true).value.shouldBeNull()
        }
    }
}
