package nebulosa.vizier

import nebulosa.retrofit.RetrofitService
import okhttp3.FormBody
import okhttp3.ResponseBody
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * @see <a href="http://cdsarc.u-strasbg.fr/doc/asu-summary.htx">Documentation</a>
 */
class VizierTAPService(url: String = URL) : RetrofitService(url) {

    override val converterFactory: List<Converter.Factory> = listOf(CSVRecordListConverterFactory)

    private val service by lazy { retrofit.create(VizierTAP::class.java) }

    fun query(query: String): Call<List<CSVRecord>> {
        val body = FormBody.Builder()
            .add("request", "doQuery")
            .add("lang", "adql")
            .add("format", "csv")
            .add("query", query)
            .build()

        return service.query(body)
    }

    private object CSVRecordListConverter : Converter<ResponseBody, List<CSVRecord>> {

        override fun convert(value: ResponseBody): List<CSVRecord> {
            val charset = value.contentType()?.charset() ?: Charsets.UTF_8
            val format = CSVFormat.Builder
                .create(CSVFormat.DEFAULT)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build()
            val parser = CSVParser.parse(value.byteStream(), charset, format)
            return parser.use { it.toList() }
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
    }
}
