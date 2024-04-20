package nebulosa.retrofit

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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

    protected val mapper by lazy { mapper ?: DEFAULT_MAPPER.copy()!! }

    protected open val converterFactory = emptyList<Converter.Factory>()

    protected open val callAdaptorFactory: CallAdapter.Factory? = null

    protected open fun handleOkHttpClientBuilder(builder: OkHttpClient.Builder) = Unit

    protected open fun handleObjectMapper(mapper: ObjectMapper) = Unit

    protected open val retrofit by lazy {
        val builder = Retrofit.Builder()
        builder.baseUrl(url.trim().let { if (it.endsWith("/")) it else "$it/" })
        builder.addConverterFactory(RawAsStringConverterFactory)
        builder.addConverterFactory(RawAsByteArrayConverterFactory)
        builder.addConverterFactory(EnumToStringConverterFactory(this.mapper))
        converterFactory.forEach { builder.addConverterFactory(it) }
        handleObjectMapper(this.mapper)
        builder.addConverterFactory(JacksonConverterFactory.create(this.mapper))
        callAdaptorFactory?.also(builder::addCallAdapterFactory)

        with((httpClient ?: HTTP_CLIENT).newBuilder()) {
            handleOkHttpClientBuilder(this)
            builder.client(build())
        }

        builder.build()
    }

    companion object {

        @JvmStatic private val CONNECTION_POOL = ConnectionPool(32, 30L, TimeUnit.MINUTES)

        @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
            .connectionPool(CONNECTION_POOL)
            .readTimeout(60L, TimeUnit.SECONDS)
            .writeTimeout(60L, TimeUnit.SECONDS)
            .connectTimeout(60L, TimeUnit.SECONDS)
            .callTimeout(60L, TimeUnit.SECONDS)
            .build()

        @JvmStatic private val DEFAULT_MAPPER = ObjectMapper()
            .registerModules(JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)!!
    }
}
