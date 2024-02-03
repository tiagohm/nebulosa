package nebulosa.retrofit

import de.siegmar.fastcsv.reader.CsvReader
import de.siegmar.fastcsv.reader.NamedCsvRecord
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.InputStreamReader

data class CSVRecordListConverter(private val reader: CsvReader.CsvReaderBuilder) : Converter<ResponseBody, List<NamedCsvRecord>> {

    override fun convert(value: ResponseBody): List<NamedCsvRecord> {
        val charset = value.contentType()?.charset() ?: Charsets.UTF_8
        return value.use { reader.ofNamedCsvRecord(InputStreamReader(value.byteStream(), charset)).toList() }
    }
}
