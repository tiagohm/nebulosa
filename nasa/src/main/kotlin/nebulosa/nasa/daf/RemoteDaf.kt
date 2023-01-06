package nebulosa.nasa.daf

import nebulosa.io.SeekableSource
import nebulosa.io.readDoubleArray
import nebulosa.io.source
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import org.apache.commons.codec.binary.Hex
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class RemoteDaf(
    val uri: String,
    private val cacheDirectory: Path? = null,
) : Daf() {

    override fun initialize() {
        val request = Request.Builder()
            .head().url(uri)
            .build()

        if (cacheDirectory == null) {
            HTTP_CLIENT.newCall(request).execute().use {
                if (it.code != 200) {
                    throw IllegalArgumentException("The given URL is inaccessible: $uri")
                }

                val acceptRanges = it.header("Accept-Ranges")

                if (acceptRanges != "bytes") {
                    throw IllegalArgumentException("The given URL not accept range requests: $uri")
                }
            }
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
        return if (cacheDirectory == null) {
            readSourceFromUri(start, end)
        } else {
            val hash = Hex.encodeHexString(MD5.digest(uri.toByteArray()), true)
            val filePath = Paths.get("$cacheDirectory", "$hash-$start-$end.cache")

            if (filePath.exists()) {
                // filePath.toFile().seekableSource()
                filePath.readBytes().source()
            } else {
                readSourceFromUri(start, end, filePath)
            }
        }
    }

    private fun readSourceFromUri(
        start: Long, end: Long,
        cacheFilePath: Path? = null,
    ): SeekableSource {
        val request = Request.Builder()
            .get().url(uri)
            .addHeader("Range", "bytes=$start-$end")
            .build()

        return HTTP_CLIENT.newCall(request).execute().use {
            val bytes = it.body.bytes()
            cacheFilePath?.writeBytes(bytes)
            bytes.source()
        }
    }

    override fun close() {}

    companion object {

        @JvmStatic private val HTTP_CLIENT = OkHttpClient.Builder().build()
        @JvmStatic private val MD5 = MessageDigest.getInstance("MD5")!!
    }
}
