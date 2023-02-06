package nebulosa.query.horizons

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Horizons {

    @GET(OBSERVER_API_URL)
    fun observer(
        @Query("COMMAND") command: String,
        @Query("SITE_COORD") coordinates: String,
        @Query("START_TIME") startTime: String,
        @Query("STOP_TIME") endTime: String,
        @Query("STEP_SIZE") stepSize: String,
        @Query("QUANTITIES") quantities: String,
        @Query("APPARENT") apparent: String,
        @Query("EXTRA_PREC") extraPrecision: String,
    ): Call<HorizonsEphemeris>

    @GET(SPK_API_URL)
    fun spk(
        @Query("COMMAND") command: String,
        @Query("START_TIME") startTime: String,
        @Query("STOP_TIME") endTime: String,
    ): Call<SpkFile>

    companion object {

        const val OBSERVER_API_URL =
            "horizons.api?format=text&MAKE_EPHEM=YES&EPHEM_TYPE=OBSERVER&CENTER='coord@399'&COORD_TYPE=GEODETIC&REF_SYSTEM='ICRF'&CAL_FORMAT='CAL'&TIME_DIGITS='MINUTES'&ANG_FORMAT='DEG'&RANGE_UNITS='AU'&SUPPRESS_RANGE_RATE='YES'&SKIP_DAYLT='NO'&SOLAR_ELONG='0,180'&OBJ_DATA='NO'&CSV_FORMAT='YES'&ELEV_CUT='-90'"
        const val SPK_API_URL = "horizons.api?format=json&EPHEM_TYPE=SPK&OBJ_DATA=NO"
    }
}
