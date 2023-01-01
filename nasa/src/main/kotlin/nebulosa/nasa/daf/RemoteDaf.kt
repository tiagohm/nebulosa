package nebulosa.nasa.daf

import nebulosa.io.SeekableSource
import nebulosa.io.readDoubleArray
import nebulosa.io.source
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import java.util.concurrent.TimeUnit

class RemoteDaf(val uri: String) : Daf() {

    override fun initialize() {
        val request = Request.Builder()
            .head().url(uri)
            .build()

        HTTP_CLIENT.newCall(request).execute().use {
            if (it.code != 200) {
                throw IllegalArgumentException("The given URL is inaccessible: $uri")
            }

            val acceptRanges = it.header("Accept-Ranges")

            if (acceptRanges != "bytes") {
                throw IllegalArgumentException("The given URL not accept range requests: $uri")
            }

            super.initialize()
        }
    }

    override fun read(start: Int, end: Int): DoubleArray {
        val startIndex = 8L * (start - 1)
        val length = 1 + end - start
        val endIndex = startIndex + length * 8L - 1L
        return readSource(startIndex, endIndex).buffer().readDoubleArray(length, record.order)
    }

    override fun readRecord(index: Int): SeekableSource {
        val startIndex = (index - 1) * 1024L
        val endIndex = startIndex + 1023
        return readSource(startIndex, endIndex)
    }

    private fun readSource(start: Long, end: Long): SeekableSource {
        val request = Request.Builder()
            .get().url(uri)
            .addHeader("Range", "bytes=$start-$end")
            .build()

        return HTTP_CLIENT.newCall(request).execute().use {
            it.body.bytes().source()
        }
    }

    override fun close() {}

    companion object {

        @JvmStatic private val CONNECTION_POOL = ConnectionPool(32, 30, TimeUnit.MINUTES)

        @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder()
            .connectionPool(CONNECTION_POOL)
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
