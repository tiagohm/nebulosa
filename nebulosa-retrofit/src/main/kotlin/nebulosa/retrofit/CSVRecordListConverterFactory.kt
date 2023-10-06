package nebulosa.retrofit

import de.siegmar.fastcsv.reader.NamedCsvReader
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

data class CSVRecordListConverterFactory(private val reader: NamedCsvReader.NamedCsvReaderBuilder) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ) = CSVRecordListConverter(reader)
}