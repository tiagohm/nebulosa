package nebulosa.vizier

import de.siegmar.fastcsv.reader.NamedCsvReader
import de.siegmar.fastcsv.reader.NamedCsvRow
import nebulosa.retrofit.RetrofitService
import okhttp3.FormBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create
import java.io.InputStreamReader
import java.lang.reflect.Type

/**
 * @see <a href="http://cdsarc.u-strasbg.fr/doc/asu-summary.htx">Documentation</a>
 */
class VizierTAPService(url: String = URL) : RetrofitService(url) {

    override val converterFactory: List<Converter.Factory> = listOf(CSVRecordListConverterFactory)

    private val service by lazy { retrofit.create<VizierTAP>() }

    fun query(query: String): Call<List<NamedCsvRow>> {
        val body = FormBody.Builder()
            .add("request", "doQuery")
            .add("lang", "adql")
            .add("format", "csv")
            .add("query", query)
            .build()

        return service.query(body)
    }

    private object CSVRecordListConverter : Converter<ResponseBody, List<NamedCsvRow>> {

        override fun convert(value: ResponseBody): List<NamedCsvRow> {
            val charset = value.contentType()?.charset() ?: Charsets.UTF_8
            return value.use { CSV_READER.build(InputStreamReader(value.byteStream(), charset)).toList() }
        }
    }

    private object CSVRecordListConverterFactory : Converter.Factory() {

        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit,
        ): Converter<ResponseBody, *> {
            return CSVRecordListConverter
        }
    }

    companion object {

        const val URL = "https://tapvizier.u-strasbg.fr/"

        @JvmStatic private val CSV_READER = NamedCsvReader.builder()
            .fieldSeparator(',')
            .quoteCharacter('"')
            .commentCharacter('#')
            .skipComments(true)
    }
}
