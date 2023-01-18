package nebulosa.alpaca.api

sealed interface Device {

    fun isConnected(deviceNumber: Int): AlpacaResponse<Boolean>

    fun connect(deviceNumber: Int, connected: Boolean): AlpacaResponse<Any>
}
