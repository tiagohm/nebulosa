package nebulosa.query.sbd

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SmallBodyDatabaseLookup {

    @GET(API_URL)
    fun search(@Query("sstr") text: String): Call<SmallBody>

    companion object {

        const val API_URL =
            "sbdb.api?alt-des=1&alt-orbits=1&ca-data=1" +
                    "&ca-time=both&ca-tunc=both&cd-epoch=1&cd-tp=1&discovery=1" +
                    "&full-prec=1&nv-fmt=both&orbit-defs=1&phys-par=1&r-notes=1" +
                    "&r-observer=1&radar-obs=1&sat=1&vi-data=1&www=1"
    }
}
