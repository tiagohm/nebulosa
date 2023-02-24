package nebulosa.query

import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit

abstract class QueryService : RetrofitService {

    protected constructor(retrofit: Retrofit, mapper: ObjectMapper? = null) : super(retrofit, mapper)

    constructor(
        url: String,
        mapper: ObjectMapper? = null,
        converterFactory: Converter.Factory? = null,
        callAdaptorFactory: CallAdapter.Factory? = null,
        client: OkHttpClient? = null,
        clientBuilderHandle: (OkHttpClient.Builder) -> Unit = {},
        logLevel: HttpLoggingInterceptor.Level? = HttpLoggingInterceptor.Level.BASIC,
    ) : super(url, mapper, converterFactory, callAdaptorFactory, client, clientBuilderHandle, logLevel)
}
