package nebulosa.watney.plate.solving.quad

import nebulosa.io.ByteOrder
import nebulosa.io.SeekableSource
import nebulosa.io.readUnsignedByte
import nebulosa.watney.plate.solving.quad.QuadDatabase.Companion.INDEX_FORMAT_ID
import nebulosa.watney.plate.solving.quad.QuadDatabase.Companion.INDEX_VERSION
import okio.buffer
import java.io.Closeable

internal data class QuadDatabaseCellFileIndex(
    @JvmField val files: List<QuadDatabaseCellFile>,
    @JvmField val byteOrder: ByteOrder,
) : Closeable {

    override fun close() {
        files.map { it.descriptor.source }.toSet().forEach(Closeable::close)
    }

    companion object {

        @JvmStatic
        fun read(source: SeekableSource): QuadDatabaseCellFileIndex {
            source.seek(0L)

            val buffer = source.buffer()

            val format = buffer.readUtf8(INDEX_FORMAT_ID.length.toLong())
            require(format == INDEX_FORMAT_ID) { "invalid index format. expected $INDEX_FORMAT_ID, got $format" }
            val version = buffer.readUnsignedByte()
            require(version == INDEX_VERSION) { "invalid index version. expected $INDEX_VERSION, got $version" }

            val byteOrder = if (buffer.readUnsignedByte() == 1) ByteOrder.LITTLE
            else ByteOrder.BIG

            val cellFiles = ArrayList<QuadDatabaseCellFile>()

            while (!source.exhausted) {
                val descriptor = QuadDatabaseCellFileDescriptor.read(source, byteOrder, buffer)
                cellFiles.add(QuadDatabaseCellFile(descriptor))
            }

            buffer.close()

            return QuadDatabaseCellFileIndex(cellFiles, byteOrder)
        }
    }
}
