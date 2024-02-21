import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import nebulosa.fits.Bitpix
import nebulosa.fits.FloatImageData
import nebulosa.fits.SeekableSourceImageData
import nebulosa.io.sink
import nebulosa.io.source
import java.nio.ByteBuffer

class ImageDataTest : StringSpec() {

    init {
        "float:read" {
            val input = FloatArray(100) { it.toFloat() }
            val data = FloatImageData(10, 10, input)

            var i = 0

            data.read { b ->
                repeat(10) { b.getFloat() shouldBeExactly input[i++] }
            }

            i shouldBeExactly 100
        }
        "float:write" {
            val input = FloatArray(100) { it.toFloat() }
            val data = FloatImageData(10, 10, input)
            val output = ByteArray(100 * 4)

            data.writeTo(output.sink())

            val buffer = ByteBuffer.wrap(output)

            repeat(100) {
                buffer.getFloat() shouldBeExactly input[it]
            }
        }
        "seekable source:read" {
            val input = ByteArray(100) { it.toByte() }
            val data = SeekableSourceImageData(input.source(), 0L, 10, 10, Bitpix.BYTE)

            var i = 0

            data.read { b ->
                repeat(10) { b.get().toInt() shouldBeExactly input[i++].toInt() }
            }

            i shouldBeExactly 100
        }
        "seekable source:write" {
            val input = ByteArray(100) { it.toByte() }
            val data = SeekableSourceImageData(input.source(), 0L, 10, 10, Bitpix.BYTE)
            val output = ByteArray(input.size)

            data.writeTo(output.sink())

            repeat(output.size) {
                output[it].toInt() shouldBeExactly input[it].toInt()
            }
        }
    }
}
