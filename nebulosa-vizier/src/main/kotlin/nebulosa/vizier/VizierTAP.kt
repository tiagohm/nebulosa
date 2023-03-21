package nebulosa.vizier

import okhttp3.FormBody
import org.apache.commons.csv.CSVRecord
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface VizierTAP {

    @POST("TAPVizieR/tap/sync")
    fun query(@Body body: FormBody): Call<List<CSVRecord>>
}
