package nebulosa.watney.platesolver.quad

import nebulosa.math.Angle

interface QuadDatabase {

    fun quads(
        centerRA: Angle, centerDEC: Angle,
        radius: Angle,
        quadsPerSqDegree: Int,
        quadDensityOffsets: IntArray,
        numSubSets: Int, subSetIndex: Int,
        imageQuads: List<StarQuad>,
    ): List<StarQuad>

    companion object {

        const val FORMAT_ID = "WATNEYQDB"
        const val FORMAT_VERSION = 3
        const val INDEX_FORMAT_ID = "${FORMAT_ID}INDEX"
        const val INDEX_VERSION = 1
    }
}
