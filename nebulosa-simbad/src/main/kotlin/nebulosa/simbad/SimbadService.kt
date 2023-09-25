package nebulosa.simbad

import de.siegmar.fastcsv.reader.NamedCsvReader
import de.siegmar.fastcsv.reader.NamedCsvRow
import nebulosa.adql.Query
import nebulosa.log.loggerFor
import nebulosa.retrofit.CSVRecordListConverterFactory
import nebulosa.retrofit.RetrofitService
import okhttp3.FormBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.create

/**
 * @see <a href="https://simbad.u-strasbg.fr/simbad/tap/tapsearch.html">Tables</a>
 * @see <a href="https://simbad.u-strasbg.fr/simbad/tap/help/adqlHelp.html">ADQL Cheat sheet</a>
 * @see <a href="http://simbad.u-strasbg.fr/guide/otypes.htx">Object types</a>
 */
class SimbadService(
    url: String = "https://simbad.u-strasbg.fr/",
    httpClient: OkHttpClient? = null,
) : RetrofitService(url, httpClient) {

    override val converterFactory = listOf(CSVRecordListConverterFactory(CSV_READER))

    private val service by lazy { retrofit.create<Simbad>() }

    fun query(query: Query): Call<List<NamedCsvRow>> {
        val body = FormBody.Builder()
            .add("request", "doQuery")
            .add("lang", "adql")
            .add("format", "tsv")
            .add("query", "$query")
            .build()

        LOG.info("query={}", query)

        return service.query(body)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<SimbadService>()

        @JvmStatic private val CSV_READER = NamedCsvReader.builder()
            .fieldSeparator('\t')
            .quoteCharacter('"')
            .commentCharacter('#')
            .skipComments(true)
    }
}
