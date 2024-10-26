package nebulosa.vizier

import de.siegmar.fastcsv.reader.CommentStrategy
import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.NamedCsvRecord
import nebulosa.retrofit.CSVRecordListConverterFactory
import nebulosa.retrofit.RetrofitService
import okhttp3.FormBody
import retrofit2.Call
import retrofit2.create

/**
 * VizieR provides the most complete library of published astronomical catalogues
 * with verified and enriched data, accessible via multiple interfaces.
 *
 * Query tools allow the user to select relevant data tables and to extract and format
 * records matching given criteria.
 *
 * @see <a href="http://cdsarc.u-strasbg.fr/doc/asu-summary.htx">Documentation</a>
 */
class VizierTAPService(url: String = "") : RetrofitService(url.ifBlank { URL }) {

    override val converterFactory = listOf(CSVRecordListConverterFactory(CSV_READER))

    private val service by lazy { retrofit.create<VizierTAP>() }

    fun query(query: String): Call<List<NamedCsvRecord>> {
        val body = FormBody.Builder()
            .add("request", "doQuery")
            .add("lang", "adql")
            .add("format", "csv")
            .add("query", query)
            .build()

        return service.query(body)
    }

    companion object {

        const val URL = "http://tapvizier.cds.unistra.fr/"

        private val CSV_READER = CsvReader.builder()
            .fieldSeparator(',')
            .quoteCharacter('"')
            .commentCharacter('#')
            .commentStrategy(CommentStrategy.SKIP)
    }
}
