package nebulosa.fits

import nebulosa.io.SeekableSource
import nebulosa.io.sink
import nebulosa.io.transferFully
import okio.Buffer
import okio.Sink
import java.nio.ByteBuffer

data class SeekableSourceImageData(
    private val source: SeekableSource,
    private val position: Long,
    override val width: Int,
    override val height: Int,
    override val bitpix: Bitpix,
) : ImageData {

    private val strideSizeInBytes = (width * bitpix.byteSize).toLong()

    override fun read(block: (ByteBuffer) -> Unit) {
        val data = ByteArray(strideSizeInBytes.toInt())
        val sink = data.sink()

        synchronized(source) {
            source.seek(position)

            Buffer().use { b ->
                repeat(height) {
                    sink.seek(0L)

                    b.transferFully(source, sink, strideSizeInBytes)
                    block(ByteBuffer.wrap(data))
                    b.clear()
                }
            }
        }
    }

    override fun writeTo(sink: Sink): Long {
        var byteCount = 0L

        return synchronized(source) {
            source.seek(position)

            Buffer().use { b ->
                repeat(height) {
                    b.transferFully(source, sink, strideSizeInBytes)
                    b.clear()
                    byteCount += strideSizeInBytes
                }
            }

            byteCount
        }
    }
}
