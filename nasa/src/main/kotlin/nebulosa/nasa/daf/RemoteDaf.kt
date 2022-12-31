package nebulosa.nasa.daf

import nebulosa.http.DEFAULT_HTTP_CLIENT
import nebulosa.io.SeekableSource
import nebulosa.io.readDoubleArray
import nebulosa.io.source
import okhttp3.Request
import okio.buffer

class RemoteDaf(val uri: String) : Daf() {

    override fun initialize() {
        val request = Request.Builder()
            .url(uri)
            .head()
            .build()

        DEFAULT_HTTP_CLIENT.newCall(request).execute().use {
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
            .url(uri)
            .get()
            .addHeader("Range", "bytes=$start-$end")
            .build()

        return DEFAULT_HTTP_CLIENT.newCall(request).execute().use {
            it.body.bytes().source()
        }
    }

    override fun close() {}
}
