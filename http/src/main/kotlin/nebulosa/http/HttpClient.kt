@file:JvmName("HttpClient")

package nebulosa.http

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@JvmField val DEFAULT_CONNECTION_POOL = ConnectionPool(32, 30, TimeUnit.MINUTES)

@JvmField val DEFAULT_HTTP_CLIENT = OkHttpClient.Builder()
    .connectionPool(DEFAULT_CONNECTION_POOL)
    .callTimeout(30, TimeUnit.SECONDS)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
