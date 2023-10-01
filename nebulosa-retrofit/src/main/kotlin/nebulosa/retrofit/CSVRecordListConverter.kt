package nebulosa.retrofit

import de.siegmar.fastcsv.reader.NamedCsvReader
import de.siegmar.fastcsv.reader.NamedCsvRow
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.InputStreamReader

data class CSVRecordListConverter(private val reader: NamedCsvReader.NamedCsvReaderBuilder) : Converter<ResponseBody, List<NamedCsvRow>> {

    override fun convert(value: ResponseBody): List<NamedCsvRow> {
        val charset = value.contentType()?.charset() ?: Charsets.UTF_8
        return value.use { reader.build(InputStreamReader(value.byteStream(), charset)).toList() }
    }
}
