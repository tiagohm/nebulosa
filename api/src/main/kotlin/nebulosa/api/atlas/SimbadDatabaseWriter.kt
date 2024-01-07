package nebulosa.api.atlas

import nebulosa.io.writeFloat
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import okio.BufferedSink
import okio.Sink
import okio.buffer
import okio.gzip
import java.io.Closeable

class SimbadDatabaseWriter(sink: Sink) : Closeable {

    private val buffer = if (sink is BufferedSink) sink else sink.gzip().buffer()

    fun write(entity: SimbadEntity) {
        write(
            entity.id, entity.name, entity.type,
            entity.rightAscensionJ2000, entity.declinationJ2000,
            entity.magnitude, entity.pmRA, entity.pmDEC,
            entity.parallax, entity.radialVelocity, entity.redshift,
            entity.constellation,
        )
    }

    @Suppress("UNUSED_PARAMETER")
    fun write(
        id: Long,
        name: String,
        type: SkyObjectType,
        rightAscensionJ2000: Double,
        declinationJ2000: Double,
        magnitude: Double,
        pmRA: Double,
        pmDEC: Double,
        parallax: Double,
        radialVelocity: Double,
        redshift: Double,
        constellation: Constellation,
    ) {
        buffer.writeLong(id)
        val encodedName = name.encodeToByteArray()
        buffer.writeShort(encodedName.size)
        buffer.write(encodedName)
        buffer.writeByte(type.ordinal)
        buffer.writeFloat(rightAscensionJ2000.toFloat())
        buffer.writeFloat(declinationJ2000.toFloat())
        buffer.writeFloat(magnitude.toFloat())
        buffer.writeFloat(pmRA.toFloat())
        buffer.writeFloat(pmDEC.toFloat())
        buffer.writeFloat(parallax.toFloat())
        buffer.writeFloat(radialVelocity.toFloat())
        // buffer.writeFloat(redshift.toFloat())
        // buffer.writeByte(constellation.ordinal)
    }

    override fun close() {
        buffer.flush()
        buffer.close()
    }
}
