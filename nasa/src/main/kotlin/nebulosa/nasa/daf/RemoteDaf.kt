package nebulosa.nasa.daf

import nebulosa.io.SeekableSource
import nebulosa.io.readDoubleArray
import nebulosa.io.source
import okio.buffer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RemoteDaf(val uri: String) : Daf() {

    override fun initialize() {
        val request = HttpRequest
            .newBuilder(URI(uri))
            .method("HEAD", HttpRequest.BodyPublishers.noBody())
            .build()

        val response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding())

        if (response.statusCode() != 200) {
            throw IllegalArgumentException("The given URL is inaccessible: $uri")
        }

        val acceptRanges = response.headers().firstValue("Accept-Ranges")

        if (!acceptRanges.isPresent || acceptRanges.get() != "bytes") {
            throw IllegalArgumentException("The given URL not accept range requests: $uri")
        }

        super.initialize()
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
        val request = HttpRequest
            .newBuilder(URI(uri))
            .GET()
            .header("Range", "bytes=$start-$end")
            .build()

        val response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray())
        return response.body().source()
    }

    override fun close() {}

    companion object {

        private val HTTP_CLIENT = HttpClient.newBuilder().build()
    }
}
