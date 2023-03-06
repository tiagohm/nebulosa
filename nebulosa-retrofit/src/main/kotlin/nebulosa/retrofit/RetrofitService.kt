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

abstract class RetrofitService(val url: String) {

    protected open val converterFactory = emptyList<Converter.Factory>()

    protected open val callAdaptorFactory: CallAdapter.Factory? = null

    protected open val mapper = ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)!!

    protected open val logLevel: HttpLoggingInterceptor.Level? = HttpLoggingInterceptor.Level.BASIC

    protected open fun handleOkHttpClientBuilder(builder: OkHttpClient.Builder) = Unit

    protected open val retrofit by lazy {
        val builder = Retrofit.Builder()
        builder.baseUrl(url)
        builder.addConverterFactory(RawAsStringConverterFactory)
        builder.addConverterFactory(RawAsByteArrayConverterFactory)
        converterFactory.forEach { builder.addConverterFactory(it) }
        builder.addConverterFactory(JacksonConverterFactory.create(mapper))
        callAdaptorFactory?.also(builder::addCallAdapterFactory)

        with(HTTP_CLIENT.newBuilder()) {
            logLevel?.also { addInterceptor(HttpLoggingInterceptor().setLevel(it)) }
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
    }
}
