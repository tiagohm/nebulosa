package nebulosa.time

import nebulosa.constants.DAYSEC
import nebulosa.constants.DAYSPERJY
import nebulosa.constants.MJD0
import okio.use
import java.net.URI
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import kotlin.io.path.writeBytes
import kotlin.math.abs

interface TimeDelta {

    /**
     * Computes de ΔT in seconds at [time].
     */
    fun delta(time: InstantOfTime): Double

    data class Offset(val offset: Double) : TimeDelta {

        private val spline = SingleSpline(doubleArrayOf(0.0, 1.0, offset))

        override fun delta(time: InstantOfTime) = spline.compute((time.value - 1721045.0) / DAYSPERJY)
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val finals2000 = IERSA()

            URI.create("https://maia.usno.navy.mil/ser7/finals2000A.all").toURL()
                .openConnection().getInputStream().use { finals2000.load(it) }

            val mjd = DoubleArray(finals2000.size) { finals2000[it, IERSA.Column.MJD].toDouble() }
            val dut1 = DoubleArray(finals2000.size, finals2000::dut1)

            val ttMinusUtc = DoubleArray(dut1.size) { if (it > 0 && abs(dut1[it] - dut1[it - 1]) > 0.9) 1.0 else 0.0 }

            for (i in ttMinusUtc.indices) {
                if (i > 0) {
                    ttMinusUtc[i] += ttMinusUtc[i - 1]
                    ttMinusUtc[i - 1] += 32.184 + 12.0
                }
            }

            ttMinusUtc[ttMinusUtc.size - 1] += 32.184 + 12.0

            val buffer = ByteBuffer.allocate(ttMinusUtc.size * 8 + 8)

            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.putLong(0, ttMinusUtc.size.toLong())

            repeat(ttMinusUtc.size) {
                buffer.putDouble((it + 1) * 8, mjd[it] + ttMinusUtc[it] / DAYSEC + MJD0)
            }

            Path.of("nebulosa-time/src/main/resources/DAILY_TT.dat").writeBytes(buffer.array())

            repeat(ttMinusUtc.size) {
                buffer.putDouble((it + 1) * 8, ttMinusUtc[it] - dut1[it])
            }

            Path.of("nebulosa-time/src/main/resources/DAILY_DELTA_T.dat").writeBytes(buffer.array())

            println(ttMinusUtc.size)
        }
    }
}
