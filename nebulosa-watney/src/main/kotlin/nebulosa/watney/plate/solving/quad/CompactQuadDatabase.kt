package nebulosa.watney.plate.solving.quad

import nebulosa.io.seekableSource
import nebulosa.math.Angle
import java.io.Closeable
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

class CompactQuadDatabase(path: Path) : QuadDatabase, Closeable {

    private val indexes = path.listDirectoryEntries("*.qdbindex")
        .map { QuadDatabaseCellFileIndex.read(it.seekableSource()) }
    private val fileSets = QuadDatabaseCellFileSet.from(indexes)

    override fun quads(
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        quadsPerSqDegree: Int, quadDensityOffsets: IntArray,
        numSubSets: Int, subSetIndex: Int,
        imageQuads: List<ImageStarQuad>,
    ): List<StarQuad> {
        val cellsToInclude = ArrayList<String>(SkySegmentSphere.size)

        for (cell in SkySegmentSphere) {
            if (isCellInSearchRadius(radius, centerRA, centerDEC, cell.bounds)) {
                cellsToInclude.add(cell.id)
            }
        }

        val sourceDataFileSets = ArrayList<QuadDatabaseCellFileSet>()

        for (fileSet in fileSets) {
            for (id in cellsToInclude) {
                if (fileSet.id == id) {
                    sourceDataFileSets.add(fileSet)
                    break
                }
            }
        }

        val quadListByDensity = Array(quadDensityOffsets.size) { Array<List<StarQuad>>(sourceDataFileSets.size) { emptyList() } }

        for (i in quadDensityOffsets.indices) {
            for (s in 0 until sourceDataFileSets.size) {
                val offset = quadDensityOffsets[i]

                quadListByDensity[i][s] = sourceDataFileSets[s].quadsWithinRange(
                    centerRA, centerDEC, radius, quadsPerSqDegree, offset, numSubSets, subSetIndex, imageQuads
                )
            }
        }

        return quadListByDensity
            .flatten()
            .flatten()
            .distinctBy { StarQuad.RatioBasedEqualityKey(it) }
    }

    override fun close() {
        fileSets.forEach(Closeable::close)
    }
}
