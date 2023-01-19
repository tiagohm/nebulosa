package nebulosa.alpaca.api

import retrofit2.Call

sealed interface Device {

    fun isConnected(deviceNumber: Int): Call<AlpacaResponse<Boolean>>

    fun connect(deviceNumber: Int, connected: Boolean): Call<AlpacaResponse<Any>>
}
