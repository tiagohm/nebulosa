package nebulosa.query

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

abstract class QueryService protected constructor(protected val retrofit: Retrofit) {

    constructor(
        url: String,
        mapper: ObjectMapper = OBJECT_MAPPER,
        converterFactory: Converter.Factory? = null,
        callAdaptorFactory: CallAdapter.Factory? = null,
        client: OkHttpClient? = null,
        clientBuilder: (OkHttpClient.Builder) -> Unit = {},
        logLevel: HttpLoggingInterceptor.Level? = HttpLoggingInterceptor.Level.BASIC,
    ) : this(
        Retrofit.Builder()
            .baseUrl(url)
            .also { if (converterFactory != null) it.addConverterFactory(converterFactory) }
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .also { if (callAdaptorFactory != null) it.addCallAdapterFactory(callAdaptorFactory) }
            .client(client.handle(clientBuilder, logLevel))
            .build()
    )

    companion object {

        @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(32, 30L, TimeUnit.MINUTES))
            .build()

        @JvmStatic private val OBJECT_MAPPER = ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        @JvmStatic
        private fun OkHttpClient?.handle(
            clientBuilder: (OkHttpClient.Builder) -> Unit,
            logLevel: HttpLoggingInterceptor.Level?,
        ) = (this ?: HTTP_CLIENT).newBuilder().also(clientBuilder).also { it.addLoggingInterceptor(logLevel) }.build()

        @JvmStatic
        private fun OkHttpClient.Builder.addLoggingInterceptor(level: HttpLoggingInterceptor.Level?) =
            apply { if (level != null) addInterceptor(HttpLoggingInterceptor().setLevel(level)) }
    }
}
