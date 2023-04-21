package nebulosa.skycatalog.sao

import nebulosa.io.*
import nebulosa.math.Angle.Companion.rad
import nebulosa.skycatalog.SkyCatalog
import nebulosa.skycatalog.Star
import okio.buffer
import okio.source
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

class SaoCatalog : SkyCatalog<Star>(258997) {

    fun load(
        path: Path,
        order: ByteOrder = ByteOrder.LITTLE,
    ) {
        path.inputStream().use { load(it, order) }
    }

    @Suppress("UNUSED_VARIABLE")
    fun load(
        inputStream: InputStream,
        order: ByteOrder = ByteOrder.LITTLE,
    ) {
        clear()

        val buffer = inputStream.source().buffer()

        val subtract = buffer.readInt(order)
        val firstStarNumber = buffer.readInt(order)
        val numberOfStars = buffer.readInt(order)
        val noStarIdIsPresent = buffer.readInt(order) == 0
        val properMotionIsIncluded = buffer.readInt(order) == 1
        val numberOfMagnitudes = buffer.readInt(order)
        val numberOfBytesPerEntry = buffer.readInt(order)
        var index = 1

        while (!buffer.exhausted()) {
            val id = if (noStarIdIsPresent) index++ else buffer.readInt(order)
            // TODO: Convert B1950 to J2000.
            val rightAscension = buffer.readDouble(order).rad
            val declination = buffer.readDouble(order).rad
            val spType = buffer.readString(2, Charsets.US_ASCII).trim()
            val magnitude = buffer.readShort(order) / 100.0
            val pmRA = if (properMotionIsIncluded) buffer.readFloat(order) else 0f
            val pmDEC = if (properMotionIsIncluded) buffer.readFloat(order) else 0f

            val star = Star(
                id = id,
                sao = "$id",
                rightAscension = rightAscension,
                declination = declination,
                spType = spType,
                magnitude = magnitude,
                pmRA = pmRA.toDouble().rad,
                pmDEC = pmDEC.toDouble().rad,
            )

            add(star)
        }
    }
}
