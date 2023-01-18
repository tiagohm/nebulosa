package nebulosa.alpaca.client

sealed interface Device {

    fun isConnected(deviceNumber: Int): AlpacaResponse<Boolean>

    fun connect(deviceNumber: Int, connected: Boolean): AlpacaResponse<Any>
}
