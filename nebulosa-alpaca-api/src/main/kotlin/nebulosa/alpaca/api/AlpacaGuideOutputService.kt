package nebulosa.alpaca.api

import retrofit2.Call

interface AlpacaGuideOutputService : AlpacaDeviceService {

    fun canPulseGuide(id: Int): Call<BoolResponse>

    fun isPulseGuiding(id: Int): Call<BoolResponse>

    fun pulseGuide(id: Int, direction: PulseGuideDirection, durationMs: Long): Call<NoneResponse>
}
