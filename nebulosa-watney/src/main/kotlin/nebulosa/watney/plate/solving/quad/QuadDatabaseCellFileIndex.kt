package nebulosa.watney.plate.solving.quad

import nebulosa.io.ByteOrder
import nebulosa.io.readUnsignedByte
import nebulosa.io.seekableSource
import nebulosa.watney.plate.solving.quad.QuadDatabase.Companion.INDEX_FORMAT_ID
import nebulosa.watney.plate.solving.quad.QuadDatabase.Companion.INDEX_VERSION
import okio.buffer
import java.nio.file.Path

internal data class QuadDatabaseCellFileIndex(
    @JvmField val files: List<QuadDatabaseCellFile>,
    @JvmField val byteOrder: ByteOrder,
) {

    companion object {

        @JvmStatic
        fun read(path: Path): QuadDatabaseCellFileIndex {
            return path.seekableSource().use {
                val buffer = it.buffer()

                val format = buffer.readUtf8(INDEX_FORMAT_ID.length.toLong())
                require(format == INDEX_FORMAT_ID) { "invalid index format. expected $INDEX_FORMAT_ID, got $format" }
                val version = buffer.readUnsignedByte()
                require(version == INDEX_VERSION) { "invalid index version. expected $INDEX_VERSION, got $version" }

                val byteOrder = if (buffer.readUnsignedByte() == 1) ByteOrder.LITTLE
                else ByteOrder.BIG

                val cellFiles = ArrayList<QuadDatabaseCellFile>()

                while (!it.exhausted) {
                    val descriptor = QuadDatabaseCellFileDescriptor.read(buffer, path.parent, byteOrder)
                    cellFiles.add(QuadDatabaseCellFile(descriptor))
                }

                QuadDatabaseCellFileIndex(cellFiles, byteOrder)
            }
        }
    }
}
