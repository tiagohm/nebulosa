import io.kotest.core.spec.style.StringSpec
import nebulosa.alpaca.discovery.AlpacaDiscoverer
import nebulosa.alpaca.discovery.DiscoveredDevice
import nebulosa.alpaca.discovery.DiscoveryListener

class AlpacaDiscoveryProtocolTest : StringSpec(), DiscoveryListener {

    init {
        "run" {
            val discoverer = AlpacaDiscoverer()
            discoverer.run()
        }
    }

    override fun onDeviceFound(device: DiscoveredDevice) {
        println(device)
    }
}
