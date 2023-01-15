package nebulosa.alpaca.discovery

fun interface DiscoveryListener {

    fun onDeviceFound(device: DiscoveredDevice)
}
