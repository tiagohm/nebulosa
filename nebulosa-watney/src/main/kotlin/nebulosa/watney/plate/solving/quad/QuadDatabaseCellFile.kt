package nebulosa.watney.plate.solving.quad

import nebulosa.erfa.SphericalCoordinate
import nebulosa.io.readFloat
import nebulosa.io.source
import nebulosa.math.Angle
import nebulosa.watney.plate.solving.quad.QuadDatabaseCellFileDescriptor.SubCellInfo
import okio.Buffer
import okio.Source
import okio.buffer
import kotlin.math.abs

/**
 * A class that represents a single Cell file (a file that contains quads in
 * passes for specified RA,Dec bounds, a part of the quad database).
 */
internal data class QuadDatabaseCellFile(@JvmField val descriptor: QuadDatabaseCellFileDescriptor) {

    fun quads(
        centerRA: Angle, centerDEC: Angle, angularDistance: Double,
        passIndex: Int, numSubSets: Int, subSetIndex: Int,
        imageQuads: List<ImageStarQuad>,
    ): List<StarQuad> {
        // Quads that get a match, and are within search distance.
        val matchingQuadsWithinRange = ArrayList<StarQuad>()
        // Quads that get a match.
        val matchingQuads = ArrayList<StarQuad>()

        val pass = descriptor.passes[passIndex]
        val subCellsInRangeArr = arrayOfNulls<SubCellInfo>(pass.subCells.size)
        val subCellsInRangeIndexesArr = IntArray(subCellsInRangeArr.size)
        var subCellsInRangeLen = 0

        for (p in 0 until pass.subCells.size) {
            val subCell = pass.subCells[p]
            val angularDistanceTo = SphericalCoordinate.angularDistance(subCell.centerRA, subCell.centerDEC, centerRA, centerDEC)
            if (angularDistanceTo - pass.avgSubCellRadius < angularDistance) {
                subCellsInRangeArr[subCellsInRangeLen] = subCell
                subCellsInRangeIndexesArr[subCellsInRangeLen] = p
                subCellsInRangeLen++
            }
        }

        // Pre-allocate, so that we don't need to allocate later down the road.
        val quadDataArray = DoubleArray(8)

        repeat(subCellsInRangeLen) {
            val subCellIdx = subCellsInRangeIndexesArr[it]

            // Need to identify non-sampling cases, and maintain a separate cache for them;
            // When sampling is used, we use a number of subsets (== sampling parameter value) and we
            // cache the matching quads per subset. But when the final matching is done, we need to
            // use all possible quads - but not all are cached yet, so we can't just grab them from
            // all subsets and be done with it. Instead, maintain a separate cache for the non-sampled
            // matching runs. It's just easier that way.
            // Means we build and use the separate cache for non-sampled runs but that's fine.
            val samplingBeingUsed = numSubSets > 1

            descriptor.source.seek(0L)

            descriptor.source.buffer().use { b ->
                val format = b.readUtf8(QuadDatabase.FORMAT_ID.length.toLong())
                val versionNum = b.readInt()
                println("$format $versionNum")

                descriptor.source.seek(subCellsInRangeArr[it]!!.dataStartPos)
                val subCellDataBytes = b.readByteArray(subCellsInRangeArr[it]!!.dataLengthInBytes).source()

                val quadCount = subCellsInRangeArr[it]!!.dataLengthInBytes / DATA_LENGTH
                // We will split the quadCount to numSubSets, and pick the quads in our assigned (sampling) subset.
                val quadCountPerSubSet = quadCount / numSubSets
                val startIndex = quadCountPerSubSet * subSetIndex
                val nextStartIndex = if (subSetIndex == numSubSets - 1) quadCount else startIndex + quadCountPerSubSet

                for (q in startIndex until nextStartIndex) {
                    val quad = bytesToQuadNew(subCellDataBytes, imageQuads, quadDataArray)

                    if (quad != null) {
                        matchingQuads.add(quad)

                        val distanceTo = SphericalCoordinate.angularDistance(quad.midPointX, quad.midPointY, centerRA, centerDEC)
                        if (distanceTo < angularDistance) matchingQuadsWithinRange.add(quad)
                    }
                }
            }
        }

        return matchingQuadsWithinRange
    }

    companion object {

        // RATIOS, LARGEST_DIST, COORDS
        const val DATA_LENGTH = 6L + 4 + 4 * 2

        private const val TEN_BITS = 1.0 / 1023
        private const val NINE_BITS = 1.0 / 511

        @JvmStatic
        private fun bytesToQuadNew(source: Source, tentativeMatches: List<ImageStarQuad>?, quadDataArray: DoubleArray): StarQuad? {
            val buffer = Buffer()
            require(source.read(buffer, DATA_LENGTH) == DATA_LENGTH) { "unexpected end of file" }
            val ratios = buffer.readByteArray(6L)

            // Ratios are packed; 3x 10 bit numbers, 2x 9 bit numbers.
            quadDataArray[0] = ((ratios[1].toInt() shl 8 and 0x3FF) + (ratios[0].toInt() and 0x3FF)) * TEN_BITS
            quadDataArray[1] = ((ratios[2].toInt() and 0x0F shl 6 and 0x3FF) + (ratios[1].toInt() shr 2 and 0x3FF)) * TEN_BITS
            quadDataArray[2] = ((ratios[3].toInt() and 0x3F shl 4 and 0x3FF) + (ratios[2].toInt() shr 4 and 0x3FF)) * TEN_BITS
            quadDataArray[3] = ((ratios[4].toInt() and 0x7F shl 2 and 0x1FF) + (ratios[3].toInt() shr 6 and 0x1FF)) * NINE_BITS
            quadDataArray[4] = ((ratios[5].toInt() shl 1 and 0x1FF) + (ratios[4].toInt() shr 7 and 0x1FF)) * NINE_BITS

            var starQuad: StarQuad? = null

            if (tentativeMatches.isNullOrEmpty()) {
                quadDataArray[5] = buffer.readFloat().toDouble() // LARGEST_DIST
                quadDataArray[6] = buffer.readFloat().toDouble() // RA
                quadDataArray[7] = buffer.readFloat().toDouble() // DEC
                starQuad = ImageStarQuad(quadDataArray.sliceArray(0..4), quadDataArray[5], quadDataArray[6], quadDataArray[7])
            } else {
                for ((r) in tentativeMatches) {
                    if (abs(r[0] / quadDataArray[0] - 1.0) <= 0.011
                        && abs(r[1] / quadDataArray[1] - 1.0) <= 0.011
                        && abs(r[2] / quadDataArray[2] - 1.0) <= 0.011
                        && abs(r[3] / quadDataArray[3] - 1.0) <= 0.011
                        && abs(r[4] / quadDataArray[4] - 1.0) <= 0.011
                    ) {
                        quadDataArray[5] = buffer.readFloat().toDouble() // LARGEST_DIST
                        quadDataArray[6] = buffer.readFloat().toDouble() // RA
                        quadDataArray[7] = buffer.readFloat().toDouble() // DEC
                        starQuad = ImageStarQuad(quadDataArray.sliceArray(0..4), quadDataArray[5], quadDataArray[6], quadDataArray[7])
                        break
                    }
                }
            }

            buffer.clear()

            return starQuad
        }
    }
}
