package nebulosa.alpaca.discovery

import java.net.InetAddress

data class DiscoveredDevice(
    val address: InetAddress,
    val port: Int,
)
