package nebulosa.vizier

import de.siegmar.fastcsv.reader.NamedCsvRow
import okhttp3.FormBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface VizierTAP {

    @POST("TAPVizieR/tap/sync")
    fun query(@Body body: FormBody): Call<List<NamedCsvRow>>
}
