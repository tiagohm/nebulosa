package nebulosa.retrofit

import com.fasterxml.jackson.annotation.JsonInclude
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

abstract class RetrofitService {

    protected val retrofit: Retrofit
    protected val mapper: ObjectMapper

    protected constructor(
        retrofit: Retrofit,
        mapper: ObjectMapper? = null,
    ) {
        this.mapper = mapper ?: buildDefaultMapper()
        this.retrofit = retrofit
    }

    constructor(
        url: String,
        mapper: ObjectMapper? = null,
        converterFactory: Converter.Factory? = null,
        callAdaptorFactory: CallAdapter.Factory? = null,
        client: OkHttpClient? = null,
        clientBuilderHandle: (OkHttpClient.Builder) -> Unit = {},
        logLevel: HttpLoggingInterceptor.Level? = HttpLoggingInterceptor.Level.BASIC,
    ) {
        this.mapper = mapper ?: buildDefaultMapper()

        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .also { if (converterFactory != null) it.addConverterFactory(converterFactory) }
            .addConverterFactory(JacksonConverterFactory.create(this.mapper))
            .also { if (callAdaptorFactory != null) it.addCallAdapterFactory(callAdaptorFactory) }
            .client(client.handle(clientBuilderHandle, logLevel))
            .build()
    }

    companion object {

        @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(32, 30L, TimeUnit.MINUTES))
            .build()

        @JvmStatic
        private fun buildDefaultMapper() = ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)!!

        @JvmStatic
        private fun OkHttpClient?.handle(
            clientBuilder: (OkHttpClient.Builder) -> Unit,
            logLevel: HttpLoggingInterceptor.Level?,
        ) = (this ?: HTTP_CLIENT).newBuilder()
            .also { it.timeout() }
            .also(clientBuilder)
            .also { it.loggingInterceptor(logLevel) }
            .build()

        @JvmStatic
        private fun OkHttpClient.Builder.timeout() =
            readTimeout(30L, TimeUnit.SECONDS)
                .writeTimeout(30L, TimeUnit.SECONDS)
                .connectTimeout(30L, TimeUnit.SECONDS)
                .callTimeout(30L, TimeUnit.SECONDS)

        @JvmStatic
        private fun OkHttpClient.Builder.loggingInterceptor(level: HttpLoggingInterceptor.Level?) =
            apply { if (level != null) addInterceptor(HttpLoggingInterceptor().setLevel(level)) }
    }
}
