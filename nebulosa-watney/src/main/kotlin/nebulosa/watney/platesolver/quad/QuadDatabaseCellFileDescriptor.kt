package nebulosa.watney.platesolver.quad

import nebulosa.erfa.SphericalCoordinate
import nebulosa.io.ByteOrder
import nebulosa.io.readFloat
import nebulosa.io.readInt
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.watney.platesolver.quad.QuadDatabase.Companion.FORMAT_ID
import okio.BufferedSource
import java.nio.file.Path

internal data class QuadDatabaseCellFileDescriptor(
    @JvmField val path: Path,
    @JvmField val byteOrder: ByteOrder,
    @JvmField val bandIndex: Int,
    @JvmField val cellIndex: Int,
    @JvmField val passes: List<Pass>,
) {

    @JvmField val id = SkySegmentSphere.Cell.cellId(bandIndex, cellIndex)

    data class SubCellInfo(
        @JvmField val centerRA: Angle, @JvmField val centerDEC: Angle,
        @JvmField val dataLengthInBytes: Long,
        @JvmField var dataStartPos: Long = 0L,
    )

    data class Pass(
        @JvmField val quadsPerSqDeg: Double, @JvmField val subDivisions: Int,
        @JvmField val subCells: List<SubCellInfo>,
        @JvmField var avgSubCellRadius: Angle,
        @JvmField val dataBlockByteLength: Long,
    )

    companion object {

        @JvmStatic private val LOG = loggerFor<QuadDatabaseCellFileDescriptor>()

        @JvmStatic
        fun read(buffer: BufferedSource, indexDirectory: Path, byteOrder: ByteOrder): QuadDatabaseCellFileDescriptor {
            val filename = buffer.readUtf8(buffer.readByte().toLong())
            val band = buffer.readInt(byteOrder)
            val cell = buffer.readInt(byteOrder)
            val passCount = buffer.readInt(byteOrder)
            // LOG.debug { String.format("quad database index. filename=%s, band=%d, cell=%d, passCount=%d", filename, band, cell, passCount) }
            val cellReference = SkySegmentSphere[band, cell]
            val passes = ArrayList<Pass>(passCount)

            // We don't have any human readable header in v3 format, just the identifier
            // + version number and data follows immediately after, as the index data
            // is now in the index file.
            var dataStartPos = FORMAT_ID.length + 4L

            repeat(passCount) {
                val quadsPerSqDeg = buffer.readFloat(byteOrder)
                val subDivisions = buffer.readInt(byteOrder)
                val numSubCells = buffer.readInt(byteOrder)

                val subCells = ArrayList<SubCellInfo>(numSubCells)
                var dataBlockByteLength = 0L

                repeat(numSubCells) {
                    val centerRA = buffer.readFloat(byteOrder).toDouble().deg
                    val centerDEC = buffer.readFloat(byteOrder).toDouble().deg
                    val dataLengthInBytes = buffer.readInt(byteOrder).toLong()
                    val info = SubCellInfo(centerRA, centerDEC, dataLengthInBytes)
                    subCells.add(info)
                    dataBlockByteLength += dataLengthInBytes
                }

                val pass = Pass(quadsPerSqDeg.toDouble(), subDivisions, subCells, 0.0, dataBlockByteLength)
                passes.add(pass)
            }

            for (pass in passes) {
                for (info in pass.subCells) {
                    info.dataStartPos = dataStartPos
                    dataStartPos += info.dataLengthInBytes
                }

                if (pass.subCells.size == 1) {
                    val bounds = cellReference.bounds
                    pass.avgSubCellRadius = 0.5 *
                            SphericalCoordinate.angularDistance(bounds.left, bounds.top, bounds.right, bounds.bottom)
                } else {
                    val spanningDistance = SphericalCoordinate.angularDistance(
                        pass.subCells.first().centerRA, pass.subCells.first().centerDEC,
                        pass.subCells.last().centerRA, pass.subCells.last().centerDEC,
                    )

                    pass.avgSubCellRadius = spanningDistance / (pass.subDivisions - 1) / 2.0
                }
            }

            return QuadDatabaseCellFileDescriptor(Path.of("$indexDirectory", filename), byteOrder, band, cell, passes)
        }
    }
}
