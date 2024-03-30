package nebulosa.astrobin.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface Astrobin {

    @GET("api/v2/equipment/sensor")
    @Headers("Accept: application/json")
    fun sensors(@Query("page") page: Int): Call<Page<Sensor>>

    @GET("api/v2/equipment/sensor/{id}")
    @Headers("Accept: application/json")
    fun sensor(@Path("id") id: Long): Call<Sensor>

    @GET("api/v2/equipment/camera")
    @Headers("Accept: application/json")
    fun cameras(@Query("page") page: Int): Call<Page<Camera>>

    @GET("api/v2/equipment/camera/{id}")
    @Headers("Accept: application/json")
    fun camera(@Path("id") id: Long): Call<Camera>

    @GET("api/v2/equipment/telescope")
    @Headers("Accept: application/json")
    fun telescopes(@Query("page") page: Int): Call<Page<Telescope>>

    @GET("api/v2/equipment/telescope/{id}")
    @Headers("Accept: application/json")
    fun telescope(@Path("id") id: Long): Call<Telescope>
}
