package nebulosa.watney.platesolver.quad

import nebulosa.erfa.SphericalCoordinate
import nebulosa.io.*
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.watney.platesolver.quad.QuadDatabaseCellFileDescriptor.SubCellInfo
import okio.Buffer
import kotlin.math.abs

/**
 * A class that represents a single Cell file (a file that contains quads in
 * passes for specified RA,Dec bounds, a part of the quad database).
 */
internal data class QuadDatabaseCellFile(@JvmField val descriptor: QuadDatabaseCellFileDescriptor) {

    fun quads(
        centerRA: Angle, centerDEC: Angle, angularDistance: Double,
        passIndex: Int, numSubSets: Int, subSetIndex: Int,
        imageQuads: List<StarQuad>,
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
        val source = descriptor.path.seekableSource()
        val buffer = Buffer()

        source.seek(0L)
        source.read(buffer, QuadDatabase.FORMAT_ID.length + 4L)

        val format = buffer.readUtf8(QuadDatabase.FORMAT_ID.length.toLong())
        require(format == QuadDatabase.FORMAT_ID) { "invalid format. expected ${QuadDatabase.FORMAT_ID}, got $format" }
        val version = buffer.readInt(descriptor.byteOrder)
        require(version == QuadDatabase.FORMAT_VERSION) { "invalid version. expected ${QuadDatabase.FORMAT_VERSION}, got $version" }

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

            buffer.clear()
            source.seek(subCellsInRangeArr[it]!!.dataStartPos)
            buffer.readFully(source, subCellsInRangeArr[it]!!.dataLengthInBytes)

            val quadCount = subCellsInRangeArr[it]!!.dataLengthInBytes / DATA_LENGTH
            // We will split the quadCount to numSubSets, and pick the quads in our assigned (sampling) subset.
            val quadCountPerSubSet = quadCount / numSubSets
            val startIndex = quadCountPerSubSet * subSetIndex
            val nextStartIndex = if (subSetIndex == numSubSets - 1) quadCount else startIndex + quadCountPerSubSet

            for (q in startIndex until nextStartIndex) {
                val quad = bytesToQuadNew(buffer, descriptor.byteOrder, imageQuads, quadDataArray)

                if (quad != null) {
                    matchingQuads.add(quad)

                    val distanceTo = SphericalCoordinate.angularDistance(quad.midPointX, quad.midPointY, centerRA, centerDEC)
                    if (distanceTo < angularDistance) {
                        matchingQuadsWithinRange.add(quad)
                    }
                }
            }
        }

        buffer.close()
        source.close()

        return matchingQuadsWithinRange
    }

    companion object {

        // RATIOS, LARGEST_DIST, COORDS
        const val DATA_LENGTH = 6L + 4 + 4 * 2

        private const val TEN_BITS = 1.0 / 1023
        private const val NINE_BITS = 1.0 / 511

        private fun bytesToQuadNew(
            buffer: Buffer,
            byteOrder: ByteOrder,
            tentativeMatches: List<StarQuad>?,
            quadDataArray: DoubleArray,
        ): StarQuad? {
            val ratios = buffer.readByteArray(6L)

            // Ratios are packed; 3x 10 bit numbers, 2x 9 bit numbers.
            quadDataArray[0] = ((ratios[1].toInt() and 0xFF shl 8 and 0x3FF) + (ratios[0].toInt() and 0xFF and 0x3FF)) * TEN_BITS
            quadDataArray[1] = ((ratios[2].toInt() and 0x0F shl 6 and 0x3FF) + (ratios[1].toInt() and 0xFF shr 2 and 0x3FF)) * TEN_BITS
            quadDataArray[2] = ((ratios[3].toInt() and 0x3F shl 4 and 0x3FF) + (ratios[2].toInt() and 0xFF shr 4 and 0x3FF)) * TEN_BITS
            quadDataArray[3] = ((ratios[4].toInt() and 0x7F shl 2 and 0x1FF) + (ratios[3].toInt() and 0xFF shr 6 and 0x1FF)) * NINE_BITS
            quadDataArray[4] = ((ratios[5].toInt() and 0xFF shl 1 and 0x1FF) + (ratios[4].toInt() and 0xFF shr 7 and 0x1FF)) * NINE_BITS

            var starQuad: StarQuad? = null

            if (tentativeMatches.isNullOrEmpty()) {
                quadDataArray[5] = buffer.readFloat(byteOrder).toDouble().deg // LARGEST_DIST
                quadDataArray[6] = buffer.readFloat(byteOrder).toDouble().deg // RA
                quadDataArray[7] = buffer.readFloat(byteOrder).toDouble().deg // DEC
                starQuad = CellStarQuad(quadDataArray.sliceArray(0..4), quadDataArray[5], quadDataArray[6], quadDataArray[7])
            } else {
                var skip = true

                for (q in tentativeMatches.indices) {
                    val r = tentativeMatches[q].ratios

                    if (abs(r[0] / quadDataArray[0] - 1.0) <= 0.011
                        && abs(r[1] / quadDataArray[1] - 1.0) <= 0.011
                        && abs(r[2] / quadDataArray[2] - 1.0) <= 0.011
                        && abs(r[3] / quadDataArray[3] - 1.0) <= 0.011
                        && abs(r[4] / quadDataArray[4] - 1.0) <= 0.011
                    ) {
                        quadDataArray[5] = buffer.readFloat(byteOrder).toDouble().deg // LARGEST_DIST
                        quadDataArray[6] = buffer.readFloat(byteOrder).toDouble().deg // RA
                        quadDataArray[7] = buffer.readFloat(byteOrder).toDouble().deg // DEC
                        starQuad = CellStarQuad(quadDataArray.sliceArray(0..4), quadDataArray[5], quadDataArray[6], quadDataArray[7])
                        skip = false
                        break
                    }
                }

                if (skip) {
                    buffer.skip(DATA_LENGTH - 6L)
                }
            }

            return starQuad
        }
    }
}
