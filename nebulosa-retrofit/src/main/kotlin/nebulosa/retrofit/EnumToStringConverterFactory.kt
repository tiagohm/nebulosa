package nebulosa.retrofit

import com.fasterxml.jackson.databind.ObjectMapper
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

data class EnumToStringConverterFactory(private val mapper: ObjectMapper) : Converter.Factory() {

    private val converter = EnumToStringConverter(mapper)

    override fun stringConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, String>? {
        return if (type is Class<*> && type.isEnum) converter else null
    }
}
