import io.kotest.core.spec.style.StringSpec
import nebulosa.alpaca.discovery.AlpacaDiscoveryService
import nebulosa.alpaca.discovery.DiscoveredServer
import nebulosa.alpaca.discovery.DiscoveryListener

class AlpacaDiscoveryServiceTest : StringSpec(), DiscoveryListener {

    init {
        "run" {
            val discoverer = AlpacaDiscoveryService()
            discoverer.registerDiscoveryListener(this@AlpacaDiscoveryServiceTest)
            discoverer.run()
        }
    }

    override fun onServerFound(server: DiscoveredServer) {
        println(server)
    }
}
