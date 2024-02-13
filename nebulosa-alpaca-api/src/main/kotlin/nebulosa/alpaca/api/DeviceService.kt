package nebulosa.alpaca.api

import retrofit2.Call

sealed interface DeviceService {

    fun isConnected(id: Int): Call<BoolResponse>

    fun connect(id: Int, connected: Boolean): Call<NoneResponse>
}
