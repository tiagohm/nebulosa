package nebulosa.vizier

import retrofit2.http.GET
import retrofit2.http.Query

interface Vizier {

    @GET("viz-bin/asu-fits")
    fun query(
        @Query("-words") words: String?,
        @Query("-source") source: String?,
        @Query("-kw") kw: String?,
        @Query("-ucd") ucd: String?,
        @Query("--meta.all") metaAll: String,
    )
}
