package nebulosa.query.horizons

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Horizons {

    @GET(SPK_API_URL)
    fun spk(
        @Query("COMMAND") command: String,
        @Query("START_TIME") startTime: String,
        @Query("STOP_TIME") endTime: String,
    ): Call<SpkFile>

    companion object {

        const val SPK_API_URL = "horizons.api?format=json&EPHEM_TYPE=SPK&OBJ_DATA=NO"
    }
}
