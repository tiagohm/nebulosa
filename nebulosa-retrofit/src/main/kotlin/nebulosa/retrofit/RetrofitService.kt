package nebulosa.retrofit

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import nebulosa.json.PathModule
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

abstract class RetrofitService(
    url: String,
    httpClient: OkHttpClient? = null,
    mapper: ObjectMapper? = null,
) {

    protected val objectMapper: ObjectMapper by lazy { mapper ?: DEFAULT_MAPPER.also(::withJsonMapper).build() }

    protected open val converterFactory: Iterable<Converter.Factory>
        get() = emptyList()

    protected open val callAdaptorFactory: CallAdapter.Factory?
        get() = null

    protected open fun withOkHttpClient(builder: OkHttpClient.Builder) = Unit

    protected open fun withJsonMapper(mapper: JsonMapper.Builder) = Unit

    protected open val retrofit by lazy {
        val builder = Retrofit.Builder()
        builder.baseUrl(url.trim().let { if (it.endsWith("/")) it else "$it/" })
        builder.addConverterFactory(RawAsStringConverterFactory)
        builder.addConverterFactory(RawAsByteArrayConverterFactory)
        builder.addConverterFactory(EnumToStringConverterFactory(objectMapper))
        converterFactory.forEach { builder.addConverterFactory(it) }
        builder.addConverterFactory(JacksonConverterFactory.create(objectMapper))
        callAdaptorFactory?.also(builder::addCallAdapterFactory)

        with((httpClient ?: HTTP_CLIENT).newBuilder()) {
            withOkHttpClient(this)
            builder.client(build())
        }

        builder.build()
    }

    companion object {

        @JvmStatic private val CONNECTION_POOL = ConnectionPool(32, 5L, TimeUnit.MINUTES)

        @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
            .connectionPool(CONNECTION_POOL)
            .readTimeout(60L, TimeUnit.SECONDS)
            .writeTimeout(60L, TimeUnit.SECONDS)
            .connectTimeout(60L, TimeUnit.SECONDS)
            .callTimeout(60L, TimeUnit.SECONDS)
            .build()

        @JvmStatic private val DEFAULT_MAPPER = JsonMapper.builder().apply {
            addModule(JavaTimeModule()) // Why needs to be registered first? Fix "java.time.LocalDateTime not supported by default"
            addModule(PathModule())
            enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            serializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
}
