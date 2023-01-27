import io.kotest.core.spec.style.StringSpec
import nebulosa.alpaca.discovery.AlpacaDiscoveryProtocol
import nebulosa.alpaca.discovery.DiscoveryListener

class AlpacaDiscoveryServiceTest : StringSpec(), DiscoveryListener {

    init {
        "run" {
            val discoverer = AlpacaDiscoveryProtocol()
            discoverer.registerDiscoveryListener(this@AlpacaDiscoveryServiceTest)
            discoverer.run()
        }
    }

    override fun onServerFound(server: DiscoveredServer) {
        println(server)
    }
}
