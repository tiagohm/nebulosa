package nebulosa.watney.plate.solving

import nebulosa.imaging.Image
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.star.detection.ImageStar
import nebulosa.watney.plate.solving.quad.ImageStarQuad
import nebulosa.watney.star.detection.WatneyStarDetector
import java.time.Duration
import java.util.*
import kotlin.math.min

class WatneyPlateSolver(
    private val maxStars: Int = -1,
    private val sampling: Int = 4,
) : PlateSolver<Image> {

    private val starDetector = WatneyStarDetector()

    override fun solve(
        input: Image, blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
    ): PlateSolution {
        val stars = starDetector.detect(input)

        if (stars.isEmpty()) return PlateSolution.NO_SOLUTION

        val maxStars = if (maxStars > 0) maxStars else min(stars.size / 3, 1000)
        val (imageStarQuads, countInFirstPass) = formImageStarQuads(stars)

        val strategy = if (radius > 0.0) NearbySearchStrategy(centerRA, centerDEC)
        else BlindSearchStrategy()

        val searchQueue = strategy.searchQueue()

        val groupedSearchQueue = TreeMap<Double, MutableList<SearchRun>>(Collections.reverseOrder())
        val runsByRadius = searchQueue.groupByTo(groupedSearchQueue) { it.radius.toDegrees }

        return PlateSolution.NO_SOLUTION
    }

    companion object {

        @JvmStatic
        private fun formImageStarQuads(starsFound: List<ImageStar>): Pair<List<ImageStarQuad>, Int> {
            val quads = ArrayList<ImageStarQuad>()
            var countInFirstPass = 0

            // Do a few passes. Experimental.
            repeat(4) { p ->
                val starsToUse = (starsFound.size * (1 - p * 0.05)).toInt()
                val stars = starsFound.subList(0, starsToUse)

                val starDistances = Array(stars.size) { DoubleArray(stars.size) }

                for (i in stars.indices) {
                    starDistances[i][i] = 0.0

                    for (j in i + 1 until stars.size) {
                        val dist = stars[i].distance(stars[j])
                        starDistances[i][j] = dist
                        starDistances[j][i] = dist
                    }
                }

                for (i in starDistances.indices) {
                    val distancesToOthers = starDistances[i]

                    // Get 3 nearest.
                    val nearestIndices = intArrayOf(-1, -1, -1)
                    val nearestDistances = DoubleArray(nearestIndices.size)

                    for (n in 0..2) {
                        var index = 0
                        var dist = Double.MAX_VALUE

                        for (j in distancesToOthers.indices) {
                            if (distancesToOthers[j] < dist && distancesToOthers[j] > 0 && j != nearestIndices[0] && j != nearestIndices[1]) {
                                dist = distancesToOthers[j]
                                index = j
                            }
                        }

                        nearestIndices[n] = index
                        nearestDistances[n] = dist
                    }

                    val (d0a, d0b, d0c) = nearestDistances
                    val (starIndexA, starIndexB, starIndexC) = nearestIndices

                    val dab = starDistances[starIndexA][starIndexB]
                    val dac = starDistances[starIndexA][starIndexC]
                    val dbc = starDistances[starIndexB][starIndexC]

                    val sixDistances = arrayOf(d0a, d0b, d0c, dab, dac, dbc)
                    sixDistances.sort()
                    val largestDistance = sixDistances.max()

                    val ratios = DoubleArray(sixDistances.size - 1) { sixDistances[it] / largestDistance }
                    val quadStars = listOf(stars[i], stars[starIndexA], stars[starIndexB], stars[starIndexC])

                    val quad = ImageStarQuad(ratios, largestDistance.toFloat().toDouble(), quadStars)
                    quads.add(quad)
                }

                if (p == 0) {
                    countInFirstPass = quads.distinctBy { StarQuadStarBasedEqualityKey(it) }.size
                }
            }

            val quadsArray = quads.distinctBy { ImageStarQuadStarBasedEqualityKey(it) }
            return quadsArray to countInFirstPass
        }

        private class StarQuadStarBasedEqualityKey(@JvmField val quad: ImageStarQuad) {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is StarQuadStarBasedEqualityKey) return false
                if (quad === other.quad) return true
                return quad.stars.containsAll(other.quad.stars)
            }

            override fun hashCode(): Int {
                return quad.stars[0].hashCode() xor quad.stars[1].hashCode() xor
                        quad.stars[2].hashCode() xor quad.stars[3].hashCode()
            }
        }

        private class ImageStarQuadStarBasedEqualityKey(@JvmField val quad: ImageStarQuad) {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is StarQuadStarBasedEqualityKey) return false
                if (quad === other.quad) return true
                // Disallow a quad definition that has same pixel coords than another one (this is so that equations
                // won't flip when we get two slightly different ra,dec coordinates representing the same pixel)
                if (quad.midPointX == other.quad.midPointX && quad.midPointY == other.quad.midPointY) return true
                return quad.stars.containsAll(other.quad.stars)
            }

            override fun hashCode(): Int {
                return quad.stars[0].hashCode() xor quad.stars[1].hashCode() xor
                        quad.stars[2].hashCode() xor quad.stars[3].hashCode() xor
                        quad.midPointX.hashCode() xor quad.midPointY.hashCode()
            }
        }
    }
}
