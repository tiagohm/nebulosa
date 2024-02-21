import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import nebulosa.alpaca.discovery.AlpacaDiscoveryProtocol
import nebulosa.alpaca.discovery.DiscoveryListener
import nebulosa.test.NonGitHubOnlyCondition
import java.net.InetAddress
import kotlin.concurrent.thread

@EnabledIf(NonGitHubOnlyCondition::class)
class AlpacaDiscoveryProtocolTest : StringSpec(), DiscoveryListener {

    init {
        "discovery" {
            val discoverer = AlpacaDiscoveryProtocol()
            discoverer.registerDiscoveryListener(this@AlpacaDiscoveryProtocolTest)
            thread { Thread.sleep(10000); discoverer.close() }
            discoverer.run()
        }
    }

    override fun onServerFound(address: InetAddress, port: Int) {
        println("$address:$port")
    }
}
