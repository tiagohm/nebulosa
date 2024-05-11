package nebulosa.retrofit

import com.fasterxml.jackson.databind.ObjectMapper
import retrofit2.Converter

data class EnumToStringConverter(private val mapper: ObjectMapper) : Converter<Enum<*>, String> {

    override fun convert(value: Enum<*>): String? {
        return mapper.writeValueAsString(value)
    }
}
