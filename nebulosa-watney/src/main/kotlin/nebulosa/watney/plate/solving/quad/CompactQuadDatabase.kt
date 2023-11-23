package nebulosa.watney.plate.solving.quad

import nebulosa.math.Angle
import java.io.Closeable

class CompactQuadDatabase : QuadDatabase, Closeable {

    override fun quads(
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        quadsPerSqDegree: Int, quadDensityOffsets: IntArray,
        numSubSets: Int, subSetIndex: Int,
        imageQuads: List<ImageStarQuad>,
        solveContextId: String
    ): List<StarQuad> {
        return emptyList()
    }

    override fun close() {

    }
}
