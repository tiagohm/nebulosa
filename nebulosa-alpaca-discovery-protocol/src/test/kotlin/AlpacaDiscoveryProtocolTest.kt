import nebulosa.alpaca.discovery.AlpacaDiscoveryProtocol
import nebulosa.alpaca.discovery.DiscoveryListener
import nebulosa.test.NonGitHubOnly
import org.junit.jupiter.api.Test
import java.net.InetAddress
import kotlin.concurrent.thread

@NonGitHubOnly
class AlpacaDiscoveryProtocolTest : DiscoveryListener {

    @Test
    fun discovery() {
        val discoverer = AlpacaDiscoveryProtocol()
        discoverer.registerDiscoveryListener(this@AlpacaDiscoveryProtocolTest)
        thread { Thread.sleep(10000); discoverer.close() }
        discoverer.run()
    }

    override fun onServerFound(address: InetAddress, port: Int) {
        println("$address:$port")
    }
}
