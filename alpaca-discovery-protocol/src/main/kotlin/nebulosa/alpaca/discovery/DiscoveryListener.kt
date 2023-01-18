package nebulosa.alpaca.discovery

fun interface DiscoveryListener {

    fun onServerFound(server: DiscoveredServer)
}
