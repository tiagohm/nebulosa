package nebulosa.api.atlas

import nebulosa.io.readFloat
import nebulosa.math.deg
import nebulosa.math.kms
import nebulosa.math.mas
import nebulosa.skycatalog.SkyObject
import nebulosa.skycatalog.SkyObjectType
import okio.BufferedSource
import okio.Source
import okio.buffer
import okio.gzip

class SkyDatabaseReader(source: Source) : Iterator<SkyObjectEntity>, AutoCloseable {

    private val buffer = if (source is BufferedSource) source else source.gzip().buffer()

    override fun hasNext() = !buffer.exhausted()

    override fun next(): SkyObjectEntity {
        val id = buffer.readLong()
        val byteCount = buffer.readShort().toLong() and 0xFFFF
        val name = buffer.readString(byteCount, Charsets.UTF_8).split(SkyDatabaseWriter.NAME_SEPARATOR)
        val type = SkyObjectType.entries[buffer.readByte().toInt() and 0xFF]
        val rightAscension = buffer.readFloat().toDouble().deg
        val declination = buffer.readFloat().toDouble().deg
        val magnitude = buffer.readFloat().toDouble()
        val pmRA = buffer.readFloat().toDouble().mas
        val pmDEC = buffer.readFloat().toDouble().mas
        val parallax = buffer.readFloat().toDouble().mas
        val radialVelocity = buffer.readFloat().toDouble().kms
        val redshift = 0.0 // buffer.readDouble()
        // val constellation = Constellation.entries[buffer.readByte().toInt() and 0xFF]
        val constellation = SkyObject.constellationFor(rightAscension, declination)

        return SkyObjectEntity(id, name, type, rightAscension, declination, magnitude, pmRA, pmDEC, parallax, radialVelocity, redshift, constellation)
    }

    override fun close() {
        buffer.close()
    }
}
