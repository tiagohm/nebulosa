package nebulosa.simbad

import de.siegmar.fastcsv.reader.NamedCsvRow
import okhttp3.FormBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Simbad {

    @POST("simbad/sim-tap/sync")
    fun query(@Body body: FormBody): Call<List<NamedCsvRow>>
}
