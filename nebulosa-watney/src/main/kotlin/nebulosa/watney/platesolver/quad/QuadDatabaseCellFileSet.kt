package nebulosa.watney.platesolver.quad

import nebulosa.math.Angle
import kotlin.math.abs

/**
 * Class representing a set of Cell files. The quad database is split into cell files.
 */
internal data class QuadDatabaseCellFileSet(
    @JvmField val id: String,
    @JvmField val files: List<QuadDatabaseCellFile>,
) {

    @JvmField val cell = SkySegmentSphere.withId(id)
    @JvmField val densities: List<CellFilePassDensity>

    data class CellFilePassDensity(
        @JvmField val quadsPerSqDeg: Double,
        @JvmField val fileIndex: Int,
        @JvmField val passIndex: Int,
    )

    init {
        val densities = ArrayList<CellFilePassDensity>()

        for (i in files.indices) {
            for (p in files[i].descriptor.passes.indices) {
                val pass = files[i].descriptor.passes[p]
                densities.add(CellFilePassDensity(pass.quadsPerSqDeg, i, p))
            }
        }

        densities.sortBy { it.quadsPerSqDeg }

        this.densities = densities
    }

    fun quadsWithinRange(
        centerRA: Angle, centerDEC: Angle, angularDistance: Angle,
        quadsPerSqDegree: Int, passOffset: Int, numSubSets: Int, subSetIndex: Int,
        imageQuads: List<StarQuad>,
    ): List<StarQuad> {
        if (passOffset > densities.size || passOffset < -densities.size) {
            return emptyList()
        }

        var closestDensityDiff = Double.MAX_VALUE
        var bestDensityIndex = 0

        for (i in densities.indices) {
            val d = abs(densities[i].quadsPerSqDeg - quadsPerSqDegree)

            if (d < closestDensityDiff) {
                bestDensityIndex = i
                closestDensityDiff = d
            }
        }

        val chosenIndex = bestDensityIndex + passOffset

        if (passOffset < 0 && chosenIndex < 0) return emptyList()
        if (passOffset > 0 && chosenIndex >= densities.size) return emptyList()

        val chosenPassDensity = densities[chosenIndex]

        return files[chosenPassDensity.fileIndex]
            .quads(centerRA, centerDEC, angularDistance, chosenPassDensity.passIndex, numSubSets, subSetIndex, imageQuads)
    }

    companion object {

        @JvmStatic
        fun from(indexes: List<QuadDatabaseCellFileIndex>): List<QuadDatabaseCellFileSet> {
            val cellFiles = indexes.map { it.files }.flatten().groupBy { it.descriptor.id }
            return cellFiles.map { QuadDatabaseCellFileSet(it.key, it.value) }
        }
    }
}
