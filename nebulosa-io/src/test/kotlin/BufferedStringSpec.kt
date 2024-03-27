import io.kotest.core.spec.style.StringSpec
import nebulosa.io.writeDouble
import nebulosa.io.writeDoubleLe
import nebulosa.io.writeFloat
import nebulosa.io.writeFloatLe
import okio.Sink
import okio.buffer

abstract class BufferedStringSpec : StringSpec() {

    protected fun Sink.initialize() {
        buffer().use {
            it.writeByte(0xAB)
            it.writeShort(0xABCD)
            it.writeShortLe(0x2143)
            it.writeInt(-0x543210FF)
            it.writeIntLe(-0x789ABCDF)
            it.writeLong(-0x543210FE789ABCDFL)
            it.writeLongLe(-0x350145414F4EA400L)
            it.writeFloat(3.14F)
            it.writeFloatLe(3.14F)
            it.writeDouble(3.14)
            it.writeDoubleLe(3.14)
            it.writeUtf8("təˈranəˌsôr")
            it.writeUtf8CodePoint("µ".codePointAt(0))
            it.writeString("c", charset = Charsets.UTF_32BE)
            it.write(byteArrayOf(1, 2, 3))
            it.writeByte(0xF9)
            it.flush()
        }
    }
}
