package nebulosa.skycatalog.sao

import nebulosa.io.*
import nebulosa.math.rad
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.ICRF
import nebulosa.skycatalog.SkyCatalog
import nebulosa.time.TimeJD
import okio.BufferedSource
import okio.Source
import okio.buffer

class SaoCatalog : SkyCatalog<SaoEntry>(258997) {

    @Suppress("UNUSED_VARIABLE")
    fun load(
        source: Source,
        order: ByteOrder = ByteOrder.LITTLE,
    ) {
        clear()

        (source as? BufferedSource ?: source.buffer()).use {
            val subtract = it.readInt(order)
            val firstStarNumber = it.readInt(order)
            val numberOfStars = it.readInt(order)
            val noStarIdIsPresent = it.readInt(order) == 0
            val properMotionIsIncluded = it.readInt(order) == 1
            val numberOfMagnitudes = it.readInt(order)
            val numberOfBytesPerEntry = it.readInt(order)
            var index = 1L

            while (!it.exhausted()) {
                val id = if (noStarIdIsPresent) index++ else it.readInt(order).toLong()
                val rightAscension = it.readDouble(order).rad
                val declination = it.readDouble(order).rad
                val spType = it.readString(2, Charsets.US_ASCII).trim()
                val magnitude = it.readShort(order) / 100.0
                val pmRA = if (properMotionIsIncluded) it.readFloat(order) else 0f
                val pmDEC = if (properMotionIsIncluded) it.readFloat(order) else 0f
                val icrf = ICRF.equatorial(rightAscension, declination, time = TimeJD.J2000, epoch = TimeJD.B1950)
                val (rightAscensionJ2000, declinationJ2000) = icrf.equatorialAtDate()

                val star = SaoEntry(
                    id, listOf("SAO $id"),
                    magnitude,
                    rightAscensionJ2000, declinationJ2000,
                    spType,
                    pmRA.rad, pmDEC.rad,
                    constellation = Constellation.find(icrf),
                )

                add(star)
            }
        }
    }
}
