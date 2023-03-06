package nebulosa.retrofit

import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object RawAsByteArrayConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ) = if (annotations.any { it.annotationClass === RawAsByteArray::class }) ByteArrayConverter else null
}
