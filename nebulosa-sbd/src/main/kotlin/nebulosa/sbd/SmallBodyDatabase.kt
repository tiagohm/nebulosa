package nebulosa.sbd

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

internal interface SmallBodyDatabase {

    @GET(
        "sbdb.api?alt-des=1&alt-orbits=1&ca-data=1" +
                "&ca-time=both&ca-tunc=both&cd-epoch=1&cd-tp=1&discovery=1" +
                "&full-prec=1&nv-fmt=both&orbit-defs=1&phys-par=1&r-notes=1" +
                "&r-observer=1&radar-obs=1&sat=1&vi-data=1&www=1"
    )
    fun search(@Query("sstr") text: String): Call<SmallBody>

    @GET("sb_ident.api?mag-required=true&two-pass=true&suppress-first-pass=true")
    fun identify(
        @Query("obs-time") dateTime: String,
        @Query("lat") lat: Double, @Query("lon") lon: Double, @Query("alt") alt: Double,
        @Query("fov-ra-center") fovRA: String, @Query("fov-dec-center") fovDEC: String,
        @Query("fov-ra-hwidth") fovRAWidth: Double, @Query("fov-dec-hwidth") fovDECWidth: Double = fovRAWidth,
        @Query("vmag-lim") magLimit: Double = 12.0,
    ): Call<SmallBodyIdentified>

    @GET("cad.api?neo=false&diameter=true")
    fun closeApproaches(
        @Query("date-min") dateMin: String, @Query("date-max") dateMax: String, @Query("dist-max") distMax: String,
    ): Call<SmallBodyIdentified>
}
