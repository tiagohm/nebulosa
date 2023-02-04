package nebulosa.query

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

abstract class QueryService protected constructor(protected val retrofit: Retrofit) {

    constructor(
        url: String,
        mapper: ObjectMapper = OBJECT_MAPPER,
        callAdaptorFactory: CallAdapter.Factory? = null,
        client: OkHttpClient? = null,
        clientBuilder: (OkHttpClient.Builder) -> Unit = {},
    ) : this(
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .also { if (callAdaptorFactory != null) it.addCallAdapterFactory(callAdaptorFactory) }
            .client((client ?: HTTP_CLIENT).newBuilder().also(clientBuilder).build())
            .build()
    )

    companion object {

        @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(32, 5L, TimeUnit.MINUTES))
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()

        @JvmStatic private val OBJECT_MAPPER = ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
}
