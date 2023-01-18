import io.kotest.core.spec.style.StringSpec
import nebulosa.alpaca.discovery.AlpacaDiscoveryService
import nebulosa.alpaca.discovery.DiscoveredServer
import nebulosa.alpaca.discovery.DiscoveryListener

class AlpacaDiscoveryProtocolTest : StringSpec(), DiscoveryListener {

    init {
        "run" {
            val discoverer = AlpacaDiscoveryService()
            discoverer.run()
        }
    }

    override fun onServerFound(server: DiscoveredServer) {
        println(server)
    }
}
