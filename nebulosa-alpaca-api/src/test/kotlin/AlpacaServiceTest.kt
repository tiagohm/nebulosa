import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import nebulosa.alpaca.api.AlpacaService

class AlpacaServiceTest : StringSpec() {

    init {
        val client = AlpacaService("https://virtserver.swaggerhub.com/ASCOMInitiative/api/v1/")

        "camera" {
            client.camera.isConnected(0).execute().body()!!.value!!.shouldBeTrue()
            client.camera.connect(0, true).execute().body()!!.value.shouldBeNull()
        }
        "telescope" {
            client.telescope.isConnected(0).execute().body()!!.value!!.shouldBeTrue()
            client.telescope.connect(0, true).execute().body()!!.value.shouldBeNull()
        }
    }
}
