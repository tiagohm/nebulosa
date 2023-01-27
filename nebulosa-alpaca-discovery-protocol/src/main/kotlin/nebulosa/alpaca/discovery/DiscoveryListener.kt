package nebulosa.alpaca.discovery

import java.net.InetAddress

fun interface DiscoveryListener {

    fun onServerFound(address: InetAddress, port: Int)
}
