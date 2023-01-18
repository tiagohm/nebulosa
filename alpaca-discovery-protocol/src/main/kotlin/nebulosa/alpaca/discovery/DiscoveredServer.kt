package nebulosa.alpaca.discovery

import java.net.InetAddress

data class DiscoveredServer(
    val address: InetAddress,
    val port: Int,
)
